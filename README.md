How to install & build & run
----------------------------

1. Clone repo: `git clone https://github.com/dkalashnik/taskM02`
2. Install Java and Gradle, ubuntu example: `apt install openjdk-8-jdk gradle -y`
3. Change dir to repo: `cd taskM02/`
4. Build jar: `gradle jar`
5. Run tests: `java -jar build/libs/test_framework-0.1.0.jar -i ./test_data/ -o result.txt`
6. Check result: `vim result.txt`
