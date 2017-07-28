package org.bbz.stock.quanttrader.http.handler.auth;

import com.google.common.reflect.ClassPath;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.http.handler.auth.anno.RequirePermissions;
import org.bbz.stock.quanttrader.http.handler.auth.anno.RequireRoles;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 利用jwt自定义的权限检测实现，
 */
@Slf4j
public class CustomJWTAuthHandlerImpl extends AuthHandlerImpl implements JWTAuthHandler{
    private static final String HANDLER_PACKAGE_BASE = "http.handler";
    /**
     * 仅供内部使用，原则上初始化之后不允许修改，否则可能造成多线程竞争，如果需要修改，可考虑采用vertx.sharedData()
     */
    private static final Map<String, PermisstionAndRoleSet> authMap = new HashMap<>();
    //    private static final Pattern BEARER = Pattern.compile( "^Bearer$", Pattern.CASE_INSENSITIVE );
    private static final String BEARER = "Bearer";
    private final JsonObject options = new JsonObject();

    static{
        try {
            ClassPath classpath = ClassPath.from( Thread.currentThread().getContextClassLoader() );
            for( ClassPath.ClassInfo classInfo : classpath.getTopLevelClassesRecursive( HANDLER_PACKAGE_BASE ) ) {
                parseClass( classInfo.load() );
            }
        } catch( IOException e ) {
            e.printStackTrace();
        }
        log.info( authMap.toString() );
    }
    private static void parseClass( Class<?> clazz ){
        final Method[] methods = clazz.getDeclaredMethods();

        for( Method method : methods ) {
            if( method.isAnnotationPresent( RequirePermissions.class ) || method.isAnnotationPresent( RequireRoles.class ) ) {
                final String clazzName = getClassName( clazz );
                PermisstionAndRoleSet roleAndPermisstionSet = new PermisstionAndRoleSet();

                if( method.isAnnotationPresent( RequirePermissions.class ) ) {
                    roleAndPermisstionSet.addPermissions( getSetFromStr( method.getDeclaredAnnotation( RequirePermissions.class ).value() ) );
                }
                if( method.isAnnotationPresent( RequireRoles.class ) ) {
                    roleAndPermisstionSet.addRoles( getSetFromStr( method.getDeclaredAnnotation( RequireRoles.class ).value() ) );
                }
                String url = clazzName + "/" + method.getName();
                authMap.put( url, roleAndPermisstionSet );
            }
        }
    }

    /**
     * 把逗号分割的字符串转成一个Set
     *
     * @param str 要分割的字符串
     * @return set
     */
    private static Set<String> getSetFromStr( String str ){
        return Arrays.stream( str.split( "," ) ).collect( Collectors.toSet() );
    }

    /**
     * 按照规则生成class的name
     * 去掉包前缀PACKAGE_BASE = "web.handler.impl"
     * 去掉类名中的Handler
     * 转换为小写
     *
     * @param clazz class
     * @return class name
     */
    private static String getClassName( Class<?> clazz ){
        String canonicalName = clazz.getCanonicalName();
        canonicalName = canonicalName.substring( HANDLER_PACKAGE_BASE.length() + 1 ).replace( "Handler", "" );
        return canonicalName.toLowerCase();
    }



    public CustomJWTAuthHandlerImpl( JWTAuth authProvider ){
//        authProvider.a
        super( authProvider );
    }

    @Override
    public JWTAuthHandler setAudience( List<String> audience ){


        options.put( "audience", new JsonArray( audience ) );
        return this;
    }

    @Override
    public JWTAuthHandler setIssuer( String issuer ){
        options.put( "issuer", issuer );
        return this;
    }

    @Override
    public JWTAuthHandler setIgnoreExpiration( boolean ignoreExpiration ){
        options.put( "ignoreExpiration", ignoreExpiration );
        return this;
    }

    @Override
    public void handle( RoutingContext context ){
        User user = context.user();
        if( user != null ) {
            // Already authenticated in, just authorise
            authorise( user, context );
        } else {
            final HttpServerRequest request = context.request();

            String token = null;

            if( request.method() == HttpMethod.OPTIONS && request.headers().get( HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS ) != null ) {
                for( String ctrlReq : request.headers().get( HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS ).split( "," ) ) {
                    if( ctrlReq.equalsIgnoreCase( "authorization" ) ) {
                        // this request has auth in access control
                        context.next();
                        return;
                    }
                }
            }
//
//            if( skip != null && context.normalisedPath().startsWith( skip ) ) {
//                context.next();
//                return;
//            }

            final String authorization = request.headers().get( HttpHeaders.AUTHORIZATION );

            if( authorization != null ) {
                String[] parts = authorization.split( " " );
                if( parts.length == 2 ) {
                    final String scheme = parts[0],
                            credentials = parts[1];

                    if( BEARER.equals( scheme ) ) {
                        token = credentials;
                    }
                } else {
                    log.warn( "Format is Authorization: Bearer [token]" );
                    context.fail( 401 );
                    return;
                }
            } else {
                log.warn( "No Authorization header was found" );
                context.fail( 401 );
                return;
            }

            JsonObject authInfo = new JsonObject().put( "jwt", token ).put( "options", options );

            authProvider.authenticate( authInfo, res -> {
                if( res.succeeded() ) {
                    final User user2 = res.result();
                    context.setUser( user2 );
                    Session session = context.session();
                    if( session != null ) {
                        // the user has upgraded from unauthenticated to authenticated
                        // session should be upgraded as recommended by owasp
                        session.regenerateId();
                    }
                    authorise( user2, context );
                } else {
                    log.warn( "JWT decode failure", res.cause() );
                    context.fail( 401 );
                }
            } );
        }
    }

    @Override
    protected void authorise( User user, RoutingContext context ){
        super.authorise( user, context );
    }


}
