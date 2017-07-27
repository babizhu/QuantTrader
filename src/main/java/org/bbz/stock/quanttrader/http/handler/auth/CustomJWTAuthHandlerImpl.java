package org.bbz.stock.quanttrader.http.handler.auth;

import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
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

import java.util.List;

/**
 * 自定义的权限检测实现
 */
@Slf4j
public class CustomJWTAuthHandlerImpl extends AuthHandlerImpl implements JWTAuthHandler{

//    private static final Pattern BEARER = Pattern.compile( "^Bearer$", Pattern.CASE_INSENSITIVE );
    private static final String  BEARER = "Bearer";
    private final JsonObject options = new JsonObject(  );

    public CustomJWTAuthHandlerImpl( JWTAuth authProvider ){
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

                    if( BEARER.equals( scheme) ) {
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
