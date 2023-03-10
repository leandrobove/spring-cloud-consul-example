package com.example.demo.controller;

import com.example.demo.properties.GithubProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/instances")
public class DiscoveryController {

    private static Logger log = LoggerFactory.getLogger(DiscoveryController.class);

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private GithubProperties githubProperties;

    @Autowired
    Environment environment;

    @GetMapping
    public List<String> getInstances() {
        return this.serviceUrl();
    }

    @GetMapping("/request")
    public String requestInstance() {
        return restTemplate.getForObject("http://ms-orders/instances/ping", String.class);
    }

    @GetMapping("/key")
    public String getDistributedKey() {
        return githubProperties.getToken();
    }

    @GetMapping("/ping")
    public Map<String, String> ping() throws UnknownHostException {

        var result = new HashMap<String, String>();

        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        String port = environment.getProperty("local.server.port");

        log.info("------------> PONG: {}", hostAddress + ":" + port);

        result.put("host", hostAddress);
        result.put("port", port);

        return result;
    }

    private List<String> serviceUrl() {
        List<ServiceInstance> list = discoveryClient.getInstances("ms-orders");

        if (list != null && list.size() > 0) {
            return list.stream().map(uri -> {
                return uri.getUri().toString();
            }).collect(Collectors.toList());
        }
        return null;
    }
}
