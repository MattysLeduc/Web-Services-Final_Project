#!/usr/bin/env bash

spring init \
--boot-version=3.4.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=patrons-service \
--package-name=com.leduc.patrons \
--groupId=com.leduc.patrons \
--dependencies=web,webflux,validation \
--version=1.0.0-SNAPSHOT \
patrons-service

spring init \
--boot-version=3.4.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=staff-service \
--package-name=com.leduc.staff \
--groupId=com.leduc.staff \
--dependencies=web,webflux,validation \
--version=1.0.0-SNAPSHOT \
staff-service

spring init \
--boot-version=3.4.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=books-service \
--package-name=com.leduc.books \
--groupId=com.leduc.books \
--dependencies=web,webflux,validation \
--version=1.0.0-SNAPSHOT \
books-service

spring init \
--boot-version=3.4.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=loans-service \
--package-name=com.leduc.loans \
--groupId=com.leduc.loans \
--dependencies=web,webflux,validation \
--version=1.0.0-SNAPSHOT \
loans-service

spring init \
--boot-version=3.4.4 \
--build=gradle \
--type=gradle-project \
--java-version=17 \
--packaging=jar \
--name=api-gateway \
--package-name=com.leduc.apigateway \
--groupId=com.leduc.apigateway \
--dependencies=web,webflux,validation,hateoas \
--version=1.0.0-SNAPSHOT \
api-gateway

