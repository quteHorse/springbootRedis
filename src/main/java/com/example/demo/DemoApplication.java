package com.example.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
//加了exclude = DataSourceAutoConfiguration.class后会报错sqlSessionFactory 或sqlSessionTemplate are required
//这是因为装配了mybatis后，mybatis本身就是用来管理数据源的一个东西，加上那个之后，mybatis的自动装配就无法完成
@SpringBootApplication
@EnableCaching

@MapperScan("com.example.demo.mapper")
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
