# About

Exposes Tesseract OCR (https://github.com/tesseract-ocr/tesseract/) calls over http as REST, so that it can be used with any programming language.

This image is built on top of Alpine 3.7 and has the *paawak/tesseract-ocr-barebones*(https://hub.docker.com/r/paawak/tesseract-ocr-barebones/) as its base image. Every attempt has been made to keep its size to a minimum. 

This application is written in Java/Spring Boot. JavaCPP-Presets(https://github.com/bytedeco/javacpp-presets) is used to talk to the C++ API of Tesseract from Java. Then, Spring Boot uses REST semantices to further expose these calls over HTTP. 

# How build docker image

mvn clean package -P docker

# How upload docker image to docker hub

mvn clean install -P docker

# How to run

## Running in Local

    java -Dspring.profiles.active=local -jar target/tesseract-ocr-rest.jar

## Running in Docker

    docker run -it -p 8080:8080    \
    -v /kaaj/installs/tesseract/tessdata_best-4.0.0:/tesseract/tessdata    \
    -v /kaaj/source/porua/tesseract-ocr-rest/images:/tesseract-temp-images   \
    -e spring.profiles.active=container     \
    paawak/tesseract-ocr-rest:latest
    
# Google Authentication

1.  Spring Security Architecture: <https://spring.io/guides/topicals/spring-security-architecture>    
1.  Google Identity Integration: <https://developers.google.com/identity/sign-in/web/backend-auth>

# Accessing the API

The REST API is at /rest/ocr. It takes in the below form data:
1. image: multipart file
2. language: for now, supports only English (value *eng*) and Bengali (value *ben*) 

## CURL

	curl -v -X POST -H "content-type:multipart/form-data" -F image=@english.png -F language=eng http://localhost:8080/rest/ocr  

## UI

	There is also a simple HTML front-end at: http://localhost:8080/
	
# Training

## Uploading a PDF eBook
This functionality lets you upload a PDF. It extracts all images into pages, sends it to OCR.

The frontend is:

    http://localhost:8080/ 
    
The REST API is:
    
    http://localhost:8080/train/pdf 
    
## Getting a list of words and their bounds:

    curl -X GET "http://localhost:8080/train/word?imagePath=/kaaj/source/porua/training/box-making-tool/src/test/resources/images/bangla-mahabharat-1-page_2.jpg&language=ben"	

## Getting a word image

The below GET request does the trick:
<http://localhost:8080/train/word/image?wordId=3&imagePath=/kaaj/source/porua/training/box-making-tool/src/test/resources/images/bangla-mahabharat-1-page_2.jpg>	
    
# DB Migration Utils with Liquibase

Reference: <https://docs.liquibase.com/tools-integrations/maven/commands/home.html>

## Manually clear all existing checksums

    mvn -P mysql liquibase:clearCheckSums

## Generate Mysql SQL Schema from existing DB

In the *pom.xml* change the *outputChangeLogFile* property in the format *schema.<target_db>.sql*. For mysql, it is *schema.mysql.sql*.

    mvn -P mysql liquibase:generateChangeLog   

## Applying liquibase changelog to MySQL DB

    mvn -P mysql liquibase:update    

## Exporting data from existing MySQL DB

Export data from tables *book* and *page_image*

    mvn -P mysql liquibase:generateChangeLog -Dliquibase.diffTypes=data -Dliquibase.diffIncludeObjects="table:book,page_image" 

Export data from table *ocr_word*

    mvn -P mysql liquibase:generateChangeLog -Dliquibase.diffTypes=data -Dliquibase.diffIncludeObjects="table:ocr_word" 
    
A file called *schema.mysql.sql* is generated with all inserts     
				
# Sources
		
<https://github.com/paawak/porua/tree/master/tesseract-ocr-rest>
