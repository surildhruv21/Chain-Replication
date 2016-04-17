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

import io.vertx.core.net.NetSocket;

public class ReplicaDriver8 extends AbstractVerticle{

	private Address successor = null;
	private Address predecessor = null;
	private String local_name = "Server8";
	private ParsedConfiguration result;
	private int server_port_no;
	public void start() {

	  	parseFile();

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
				}
				else{
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
						if(successor == null){
							client.put(query.substring(0,query.indexOf('=')),query.substring(query.indexOf('=')+1));
							req.response().setStatusCode(200);
			        		req.response().headers()
			        			.add("Content-Length", String.valueOf(16))
			            		.add("Content-Type", "text/html; charset=UTF-8");

							req.response().write("write successful");
							req.response().end();
							req.netSocket().close();
						} else {
							int sem_init_value = 0;
							final Semaphore completeWork = new Semaphore(sem_init_value); 
							HttpClient httpClient = vertx.createHttpClient();
					        httpClient.getNow(successor.port, successor.ip, uri, new Handler<HttpClientResponse>() {

					            @Override
					            public void handle(HttpClientResponse httpClientResponse) {

					                httpClientResponse.bodyHandler(new Handler<Buffer>() {
					                    @Override
					                    public void handle(Buffer buffer) {
					                        completeWork.release();
					                    }
					                });
					            }
					        });

							if(predecessor != null) {
								vertx.executeBlocking(future -> {
								try {
									completeWork.acquire();
								} catch(InterruptedException e){
									e.printStackTrace();
								}
								future.complete();
								}, res -> {
									client.put(query.substring(0,query.indexOf('=')),query.substring(query.indexOf('=')+1));
									req.response().setStatusCode(200);
					        		req.response().headers()
					        			.add("Content-Length", String.valueOf(16))
					            		.add("Content-Type", "text/html; charset=UTF-8");

									req.response().write("write successful");
									req.response().end();
									req.netSocket().close();
								}); 
							} else {
								try {
									throw new Exception("Replica should have a predecessor");
								} catch(Exception e) {
									e.printStackTrace();
								}
							}

						}
						
					} else if(command.equalsIgnoreCase("delete")){
						if(successor == null){
							boolean success = client.delete(query);
							req.response().setStatusCode(200);
			        		req.response().headers()
			        			.add("Content-Length", String.valueOf(17))
			            		.add("Content-Type", "text/html; charset=UTF-8");

							req.response().write("delete successful");
							req.response().end();
							req.netSocket().close();
						} else {
							int sem_init_value = 0;
							final Semaphore completeWork = new Semaphore(sem_init_value); 
							HttpClient httpClient = vertx.createHttpClient();
					        httpClient.getNow(successor.port, successor.ip, uri, new Handler<HttpClientResponse>() {

					            @Override
					            public void handle(HttpClientResponse httpClientResponse) {

					                httpClientResponse.bodyHandler(new Handler<Buffer>() {
					                    @Override
					                    public void handle(Buffer buffer) {
					                        completeWork.release();
					                    }
					                });
					            }
					        });



							if(predecessor != null) {
								vertx.executeBlocking(future -> {
								try {
									completeWork.acquire();
								} catch(InterruptedException e){
									e.printStackTrace();
								}
								future.complete();
								}, res -> {
									boolean success = client.delete(query);
									req.response().setStatusCode(200);
					        		req.response().headers()
					        			.add("Content-Length", String.valueOf(17))
					            		.add("Content-Type", "text/html; charset=UTF-8");

									req.response().write("delete successful");
									req.response().end();
									req.netSocket().close();
								}); 
							} else {
								try {
									throw new Exception("Replica should ahve a predecessor");
								} catch(Exception e) {
									e.printStackTrace();
								}
							}

						}
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
		}).listen(server_port_no);
		
	}

	public void parseFile() {
		// The path of your YAML file.
		final String fileName = "../configuration.yaml";
		boolean self_identification = false;
		try {
			InputStream ios = new FileInputStream(new File(fileName));
			Constructor c = new Constructor(ParsedConfiguration.class);
			Yaml yaml = new Yaml(c);
			result = (ParsedConfiguration) yaml.load(ios);
			for (User user : result.configuration) {
				if(local_name.equalsIgnoreCase(user.name)){
					System.out.println(local_name + " " + user.name + " " + user.ip);
					self_identification = true;
					server_port_no = user.port;
					continue;
				} else {
					if(self_identification){
						successor = new Address(user.ip,user.port);
						break;
					} else {
						predecessor = new Address(user.ip,user.port);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}