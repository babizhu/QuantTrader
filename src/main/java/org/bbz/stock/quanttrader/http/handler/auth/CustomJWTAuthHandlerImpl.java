package org.bbz.stock.quanttrader.http.handler.auth;

import com.google.common.reflect.ClassPath;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.handler.impl.AuthHandlerImpl;
import lombok.extern.slf4j.Slf4j;
import org.bbz.stock.quanttrader.consts.ErrorCode;
import org.bbz.stock.quanttrader.consts.EventBusAddress;
import org.bbz.stock.quanttrader.consts.EventBusCommand;
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
    protected final EventBus eventBus;
    private static final String HANDLER_PACKAGE_BASE = "org.bbz.stock.quanttrader.http.handler";
    /**
     * 仅供内部使用，原则上初始化之后不允许修改，否则可能造成多线程竞争，如果需要修改，可考虑采用vertx.sharedData()
     */
    private static final Map<String, Set<String>> authMap = new HashMap<>();
    /**
     * 角色到权限的映射
     */
    private final Map<String, Set<String>> rolesPermissionsMap = new HashMap<>();
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
                Set<String> permisstionSet = new HashSet<>();

                if( method.isAnnotationPresent( RequirePermissions.class ) ) {
                    permisstionSet.addAll( getSetFromStr( method.getDeclaredAnnotation( RequirePermissions.class ).value() ) );
                }
//                if( method.isAnnotationPresent( RequireRoles.class ) ) {
//                    roleAndPermisstionSet.addRoles( getSetFromStr( method.getDeclaredAnnotation( RequireRoles.class ).value() ) );
//                }
                ///api/trade/getTradeInfo
                String url = clazzName + "/" + method.getName();
                authMap.put( url, permisstionSet );
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
//        canonicalName = canonicalName.substring( HANDLER_PACKAGE_BASE.length() + 1 ).replace( "Handler", "" );
        int begin = canonicalName.lastIndexOf( "." ) + 1;
        canonicalName = canonicalName.substring( begin, canonicalName.length() ).replace( "Handler", "" );
        return "/api/" + canonicalName.toLowerCase();
    }


    public CustomJWTAuthHandlerImpl( EventBus eventBus, JWTAuth authProvider ){
//        authProvider.a

        super( authProvider );
        this.eventBus = eventBus;
        initRoles();
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

        final HttpServerRequest request = context.request();

//        if( request.method() == HttpMethod.OPTIONS && request.headers().get( HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS ) != null ) {
//            for( String ctrlReq : request.headers().get( HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS ).split( "," ) ) {
//                if( ctrlReq.equalsIgnoreCase( "authorization" ) ) {
//                    // this request has auth in access control
//                    context.next();
//                    return;
//                }
//            }
//        }
//
//            if( skip != null && context.normalisedPath().startsWith( skip ) ) {
//                context.next();
//                return;
//            }

        final String authorization = request.headers().get( HttpHeaders.AUTHORIZATION );
        String token = null;

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
                final User user = res.result();
                context.setUser( user );
                Session session = context.session();
                if( session != null ) {
                    // the user has upgraded from unauthenticated to authenticated
                    // session should be upgraded as recommended by owasp
                    session.regenerateId();
                }
                authorise( user, context );
            } else {
                log.warn( "JWT decode failure", res.cause() );
                context.fail( 401 );
            }
        } );
    }

//    @Override
    protected void authorise( User user, RoutingContext ctx ){
        log.debug( "检测权限,用户的权限：" + user.principal().getJsonArray( "roles" ) );
        final String uri = ctx.request().uri();
        log.debug( "访问的地址：" + uri );
        log.debug( "需要的权限：" + authMap.get( uri ) );
//        super.authorise( user, context );
        if( doIsPermitted( user.principal().getJsonArray( "roles" ), authMap.get( uri ) ) ) {
            ctx.next();
        } else {
            ctx.put( "e", ErrorCode.USER_PERMISSION_DENY ).fail( 403 );
        }
    }


    private boolean doIsPermitted( JsonArray userRoles, Set<String> uriPermissionSet ){

//        Set<String> userRoles = user.getRoles();
//        Set<String> permissions = user.getPermissions();
//        return userRoles.contains( "admin" ) || permissionOrRole.contains( userRoles ) || permissionOrRole.contains( permissions );
        for( Object role : userRoles ) {
            if( role.equals( "admin" ) ) {
                return true;
            }
            final Set<String> permisstions = rolesPermissionsMap.get( role );
            if( permisstions == null ) {
                return false;
            }
            if( permisstions.contains( uriPermissionSet ) ) {
                return true;
            }
        }
        return false;

    }

    /**
     * 很明显一旦[角色-权限表]的数据发生变化，在集群的条件下，没有办法通知到每个verticle实例的缓存，这个问题以后在着手解决
     */
    private void initRoles(){
        DeliveryOptions options = new DeliveryOptions().addHeader( "action", EventBusCommand.DB_ROLE_QUERY.name() );
        eventBus.send( EventBusAddress.DB_ADDR, new JsonObject(), options, reply -> {
            final JsonArray roles = (JsonArray) reply.result().body();
            log.info( roles.toString() );
            roles.forEach( v -> {
                JsonObject role = (JsonObject) v;
                rolesPermissionsMap.put( role.getString( "role" ), getSetFromStr( role.getString( "permissions" ) ) );
            } );
            log.info( rolesPermissionsMap.toString() );
        } );
//
    }

    @Override
    public void parseCredentials(RoutingContext routingContext, Handler<AsyncResult<JsonObject>> handler) {

    }
}
