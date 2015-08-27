package com.jerry.travel.service;

import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.NodeBuilder;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class AbstractService {
	protected static final Logger LOG = Logger.getLogger("TEST");

	private TransportClient client;

	
	/**
	 * Returns ElasticSearch client.
	 * @return client
	 */
	public Client getClient() {
		if (this.client != null) {
			return this.client;
		}
		
		NodeBuilder builder = NodeBuilder.nodeBuilder();
		
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("gateway.type", "none")
				.build();
		
		builder.settings(settings).local(true).data(true);

         client = new TransportClient();
         client.addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
		
        return this.client;
	}
	
	public void createLocalCluster(final String clusterName) {
		NodeBuilder builder = NodeBuilder.nodeBuilder();
		
		Settings settings = ImmutableSettings.settingsBuilder()
				.put("gateway.type", "none")
				.put("cluster.name", "escluster2")
				.build();
		
		builder.settings(settings).local(false).data(true);

	}
	
	/**
	 * Create index with given name if index doesn't exist.
	 * @param index index name
	 */
	public void createIndexIfNeeded(final String index) {
		if (!existsIndex(index)) {
			getClient().admin().indices().prepareCreate(index).execute().actionGet();
		}
	}
	
	public void recreateIndex(final String index) {
		logger("Recreting index: " + index);
		if (existsIndex(index)) {
			getClient().admin().indices().prepareDelete(index).execute().actionGet();
		}
		getClient().admin().indices().prepareCreate(index).execute().actionGet();
	}
	
	public boolean existsIndex(final String index) {
		IndicesExistsResponse response = getClient().admin().indices().prepareExists(index).execute().actionGet();
		return response.isExists();		
	}
	
	protected void logger(final String msg) {
		LOG.info("* " + msg);
	}
	
//	@After
//	public void close() {
//		if(client != null) {
//			client.close();
//		}
//	}
}
