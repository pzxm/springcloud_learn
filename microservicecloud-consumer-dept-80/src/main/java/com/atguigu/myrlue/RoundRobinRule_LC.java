package com.atguigu.myrlue;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.*;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author licheng
 * @description 自定义Ribbon负载均衡算法：每个服务要求被访问五次再轮询
 * @create 2019/3/14 22:44
 */
public class RoundRobinRule_LC extends AbstractLoadBalancerRule {

    //total = 0 当total等于5时，指针才能往下走
    private int total = 0;  //总共被调用的次数，目前要求每台被调用5次
    //index = 0 当前对外提供的服务器地址
    private int currentIndex = 0; //当前提供服务的服务器

    public Server choose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            return null;
        }
        Server server = null;

        while (server == null) {
            if (Thread.interrupted()) {
                return null;
            }
            List<Server> upList = lb.getReachableServers();
            List<Server> allList = lb.getAllServers();

            int serverCount = allList.size();
            if (serverCount == 0) {
                /*
                 * No servers. End regardless of pass, because subsequent passes
                 * only get more restrictive.
                 */
                return null;
            }
            if (total < 5 ){
                server = upList.get(currentIndex);
                total++;
            } else {
                    total = 0;
                    currentIndex++;
                    if (currentIndex >= upList.size()){
                        currentIndex = 0;
                    }
            }
            if (server == null) {
                /*
                 * The only time this should happen is if the server list were
                 * somehow trimmed. This is a transient condition. Retry after
                 * yielding.
                 */
                Thread.yield();
                continue;
            }

            if (server.isAlive()) {
                return (server);
            }

            // Shouldn't actually happen.. but must be transient or a bug.
            server = null;
            Thread.yield();
        }

        return server;

    }

    @Override
    public Server choose(Object key) {
        return choose(getLoadBalancer(), key);
    }

    @Override
    public void initWithNiwsConfig(IClientConfig clientConfig) {
        // TODO Auto-generated method stub

    }
}
