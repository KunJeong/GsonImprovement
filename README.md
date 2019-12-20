# Software Improvement with Gin: a Case Study

* Create gin.jar
```
git clone https://github.com/KunJeong/GsonImprovement.git
cd GsonImprovement/gin-2.0
./gradlew build
```

* Install Gson, fix faulty test, compile and run tests
```
cd ..
git clone https://github.com/google/gson.git gson
cd gson
git checkout gson-parent-2.8.5

cp ../input/ConcurrencyTest.java gson/src/test/java/com/google/gson/functional/ConcurrencyTest.java
../apache-maven-3.6.2/mvn compile
../apache-maven-3.6.2/mvn test
```

* Put gin.jar and create\_profile\_results.csv and create\_profile\_results\_all.csv (input files) in the same repository as gson
```
cp ../gin-2.0/build/gin.jar .
cp ../input/*csv .
```

* All parameters can be viewed with:
```
java -cp gin.jar gin.util.GPRuntime_U
java -cp gin.jar gin.util.GPFix_U
java -cp gin.jar gin.util.GPMemory_U
```

* Runtime Improvement

 For runtime improvement run:
```
time java -cp gin.jar gin.util.GPRuntime -d . -p gson -x 2000  -m create_profile_results.csv -h "../apache-maven-3.6.2/" -in 21 -gn 10 -r 500 
time java -cp gin.jar gin.util.GPRuntime -d . -p gson -x 2000  -m create_profile_results_all.csv -h "../apache-maven-3.6.2/" -in 21 -gn 10 -r 67
```

* Memory Improvement

 For memory improvement run:
```
time java -cp gin.jar gin.util.GPMemory -d . -p gson -x 2000  -m create_profile_results.csv -h "../apache-maven-3.6.2/" -in 21 -gn 10 -r 500 
time java -cp gin.jar gin.util.GPMemory -d . -p gson -x 2000  -m create_profile_results_all.csv -h "../apache-maven-3.6.2/" -in 21 -gn 10 -r 67
```

* Program Repair

We have already injected the faults and created GsonBuilder_1.java ~ GsonBuilder_7.java.
For the first fault,
```
cp ../input/GsonBuilder/GsonBuilder_1.java gson/src/main/java/com/google/gson/GsonBuilder.java
```

To insert the edit manually, open gson/src/main/java/com/google/gson/GsonBuilder.java and inject the following error: 
```
587c587
<     List<TypeAdapterFactory> factories = new ArrayList<TypeAdapterFactory>(this.factories.size() + this.hierarchyFactories.size() + 3); 
---
>     List<TypeAdapterFactory> factories = new ArrayList<TypeAdapterFactory>(this.factories.size() + this.hierarchyFactories.size() - 3); 
```

You can simply change the number of file to inject different errors(e.g., GsonBuilder_2.java, GsonBuilder_3.java, ...). There are total 7 errors that we have used for program repair, and they are elaborated more in /input/GsonBuilder_InjectedFaults.txt.

Re-compile all classes after the edit. For program repair run:

```
mvn compile
time java -cp gin.jar gin.util.GPFix -d . -p gson -x 2000  -m create_profile_results.csv -h "../apache-maven-3.6.2/" -in 21 -gn 10 -et MODIFY_STATEMENT
time java -cp gin.jar gin.util.GPFix -d . -p gson -x 2000  -m create_profile_results_all.csv -h "../apache-maven-3.6.2/" -in 21 -gn 10 -et MODIFY_STATEMENT
```
