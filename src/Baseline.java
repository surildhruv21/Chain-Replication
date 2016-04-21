import java.util.Map;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import java.util.*;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileInputStream;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;

import com.hazelcast.core.*;
import com.hazelcast.config.*;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.concurrent.Semaphore;
import voldemort.client.StoreClientFactory;
import voldemort.client.SocketStoreClientFactory;
import voldemort.client.StoreClient;
import voldemort.client.ClientConfig;
import voldemort.versioning.Versioned;
import voldemort.versioning.Version;

public class Baseline extends AbstractVerticle{

	private String local_name = "Master";
	private Address successor = null;
	private Address predecessor = null;
	private ParsedConfiguration result;
	public void start() {
		String bootstrapUrl = "tcp://localhost:6666";
		StoreClientFactory factory = new SocketStoreClientFactory(new ClientConfig().setBootstrapUrls(bootstrapUrl));
	  	StoreClient<String, String> client = factory.getStoreClient("test");
		HttpServer server = vertx.createHttpServer();
		server.requestHandler(new Handler<HttpServerRequest>() {
			public void handle(HttpServerRequest req) {
				int i;
				String uri = req.uri();
				String query = req.query();
				String command = "";
				if(uri.contains("?")){
					command = uri.substring(1,uri.indexOf('?'));
				}
				if(command.equals("")){
					req.response().setStatusCode(200);
			        req.response().headers()
			            .add("Content-Length", String.valueOf(15))
			            .add("Content-Type", "text/html; charset=UTF-8");
			        req.response().write("invalid command");
			        req.netSocket().close();
			        
				} else {
					if(command.equalsIgnoreCase("get")){
						Versioned<String> version = client.get(query);
						req.response().setStatusCode(200);
			        	req.response().headers()
			        		.add("Content-Length", String.valueOf(version.getValue().length()))
			            	.add("Content-Type", "text/html; charset=UTF-8");
						req.response().write(version.getValue());
						req.response().end();
						req.netSocket().close();
					} else if(command.equalsIgnoreCase("put")){
						client.put(query.substring(0,query.indexOf('=')),query.substring(query.indexOf('=')+1));
						req.response().setStatusCode(200);
		        		req.response().headers()
		        			.add("Content-Length", String.valueOf(16))
		            		.add("Content-Type", "text/html; charset=UTF-8");
						req.response().write("write successful");
						req.response().end();
						req.netSocket().close();
						
					} else if(command.equalsIgnoreCase("delete")){
						boolean success = client.delete(query);
						req.response().setStatusCode(200);
		        		req.response().headers()
		        			.add("Content-Length", String.valueOf(17))
		            		.add("Content-Type", "text/html; charset=UTF-8");

						req.response().write("delete successful");
						req.response().end();
						req.netSocket().close();
						
					} else {
						req.response().setStatusCode(200);
			        	req.response().headers()
			        		.add("Content-Length", String.valueOf(15))
			            	.add("Content-Type", "text/html; charset=UTF-8");
						
						req.response().write("invalid request");
						req.response().end();
						req.netSocket().close();
					}
				}
			}
		}).listen(8080);
		
	}
}