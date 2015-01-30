package com;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisTest {

	public RedisTest() {
	}
	
	public static void main(String[] args) {
		
		JedisPool jedisPool = new JedisPool("localhost", 6379);
		
		Jedis jedis = jedisPool.getResource();
		
		System.out.println(jedis.get("foo"));
		jedisPool.returnResource(jedis);
		jedisPool.destroy();
	}

}
