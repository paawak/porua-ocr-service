FROM openjdk:11.0.8-jre
LABEL Palash Ray <paawak@gmail.com>
RUN useradd -ms /bin/bash ocr
RUN mkdir /tesseract-temp-images
RUN mkdir /dynamic-jpa-classes
RUN chown -R ocr:ocr /dynamic-jpa-classes
ADD target/porua-ocr-service.jar //
USER ocr
ENTRYPOINT ["java", "-classpath", "/dynamic-jpa-classes:/porua-ocr-service.jar", "org.springframework.boot.loader.JarLauncher"]
