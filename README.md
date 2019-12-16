# Software Improvement with Gin: a Case Study

* Setup
```
git clone https://github.com/KunJeong/GsonImprovement.git
cd GsonImprovement/gin-2.0
./gradlew build
cd ..
git clone https://github.com/google/gson.git gson
cd gson
git checkout gson-parent-2.8.5

cp ../input/ConcurrencyTest.java gson/src/test/java/com/google/gson/functional/ConcurrencyTest.java
mvn compile
mvn test
```

Put gin.jar and create\_profile\_results.csv and create\_profile\_results\_all.csv (input files) in the same repository as gson:
```
cp ../gin-2.0/build/gin.jar .
cp ../input/*csv .
```

All parameters can be viewed with:
```
java -cp gin.jar gin.util.GPRuntime_U
java -cp gin.jar gin.util.GPFix_U
java -cp gin.jar gin.util.GPMemory_U
```

Please note that mavenHome refers to the maven home path, e.g., if the path is "/usr/local/bin/mvn", only  "/usr/local/" should be input to the -h parameter. 

* Runtime Improvement

 For runtime improvement run:
```
time java -cp gin.jar gin.util.GPRuntime -d . -p gson -x 2000  -m create_profile_results.csv -h <mavenHome> -in 21 -gn 10 -r 500 
time java -cp gin.jar gin.util.GPRuntime -d . -p gson -x 2000  -m create_profile_results_all.csv -h <mavenHome> -in 21 -gn 10 -r 67
```

* Memory Improvement

 For memory improvement run:
```
time java -cp gin.jar gin.util.GPMemory -d . -p gson -x 2000  -m create_profile_results.csv -h <mavenHome> -in 21 -gn 10 -r 500 
time java -cp gin.jar gin.util.GPMemory -d . -p gson -x 2000  -m create_profile_results_all.csv -h <mavenHome> -in 21 -gn 10 -r 67
```

* Program Repair

Edit gson/src/main/java/com/google/gson/GsonBuilder.java and inject the following error: 
```
587c587
<     List<TypeAdapterFactory> factories = new ArrayList<TypeAdapterFactory>(this.factories.size() + this.hierarchyFactories.size() + 3); 
---
>     List<TypeAdapterFactory> factories = new ArrayList<TypeAdapterFactory>(this.factories.size() + this.hierarchyFactories.size() - 3); 
```
or simply:
```
cp ../input/GsonBuilder/GsonBuilder_1.java gson/src/main/java/com/google/gson/GsonBuilder.java
```
since GsonBuilder_1.java has same injected error as above. You can simply change the number of file to inject different error(e.g., GsonBuilder_2.java, GsonBuilder_3.java, ...). There are total 7 errors that we have used for program repair, and they are elaborated more in /input/GsonBuilder_InjectedFaults.txt.

Re-compile all classes after the edit. For program repair run:

```
mvn compile
time java -cp gin.jar gin.util.GPFix -d . -p gson -x 2000  -m create_profile_results.csv -h <mavenHome> -in 21 -gn 10 -et MODIFY_STATEMENT
time java -cp gin.jar gin.util.GPFix -d . -p gson -x 2000  -m create_profile_results_all.csv -h <mavenHome> -in 21 -gn 10 -et MODIFY_STATEMENT
```

Edit gson/src/main/java/com/google/gson/GsonBuilder.java, remove previous bug, and inject the following error: 
```
588d587
<     factories.addAll(this.factories);
589a589
>     factories.addAll(this.factories);
```
or simply:
```
cp ../input/GsonBuilder/GsonBuilder_6.java gson/src/main/java/com/google/gson/GsonBuilder.java
```

Re-compile all classes after the edit. For program repair run:
```
mvn compile
time java -cp gin.jar gin.util.GPFix -d . -p gson -x 2000  -m create_profile_results.csv -h <mavenHome> -gn 10 -in 21
time java -cp gin.jar gin.util.GPFix -d . -p gson -x 2000  -m create_profile_results_all.csv -h <mavenHome> -gn 10 -in 21
```

Edit gson/src/main/java/com/google/gson/GsonBuilder.java, remove previous bug, and inject the following error: 
```
589a590
>     Collections.reverse(factories);
```
or simply:
```
cp ../input/GsonBuilder/GsonBuilder_7.java gson/src/main/java/com/google/gson/GsonBuilder.java
```

Re-compile all classes after the edit. For program repair run:
```
mvn compile
time java -cp gin.jar gin.util.GPFix -d . -p gson -x 2000  -m create_profile_results.csv -h <mavenHome> -gn 10 -in 21
time java -cp gin.jar gin.util.GPFix -d . -p gson -x 2000  -m create_profile_results_all.csv -h <mavenHome> -gn 10 -in 21
```
