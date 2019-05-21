# Project-Mani
A experimental research project for Doc-Doc similarity comparison powered by topology
> **Caution! This is a technique preview version of Mani for academic reproduction purpose. The code and all materials that mentioned in this repository are for academic research purpose only, Do not use them
in any commercial scenario!** 
### What is Mani
Mani is an experimental implementation of a novel document representation. It's unlike the widely used term-collection-based document representaion approaches. Project Mani is a graph-based representation framework. The goal of this project is to find a better representation approach which can universally preserve both lexical and syntactic semantics of documents. The preliminary results showed that the performance of the project are highly consist with human judgements.  
### How to use Mani
> **This is an experimental project, the source code of the project has been written with my personal RAD programming style. The coupling of the code is high, and the code is not easy to understang.**
  1. Download the repository then load it with IntelliJ IDE
  2. Modify the /config/babelnet.properties file with your own BabelNet API key or with the path of the offline indices
  3. Modify the /config/babelfy.properties file with your own Babelfy API key
  4. Download ADW signature Files and unzip it to /media folder
  5. Check /src/ProjectRunner.java files for examples
  
### Corpus and results
The corpus used in Mani is Dr.Michael D Lee's 50 documents, located in ./corpus folder, and all results that based on the corpus are located in ./results folder. 
> **Please notice that the Babelfy only provide online version and latest version of BabelNet is 4.01. 3.7 version of offline index has been removed from the official website. All results provided in ./results folder are based on the 3.7 version of Babelfy and BabelNet.**
### Dependent library and data
The rights of dependent libraries, toolkits and data are belong to their developers or organizations. 
##### JavaPlex
- [Persistent Homology and Topological Data Analysis Library](https://github.com/appliedtopology/javaplex/files/2196392/javaplex-processing-lib-4.3.4.zip)
##### BabelNet
- [BabelNet Java Online and Offline API 3.7.1](http://babelnet.org/data/3.7/BabelNet-API-3.7.1.zip)
- [BabelNet Offline Indices](https://babelnet.org/guide#HowcanIdownloadtheBabelNetindices?)
##### ADW
- [ADW WordNet Word Similarity Matrix 1.0](http://lcl.uniroma1.it/adw/jar/adw.v1.0.tar.gz)
- [ADW Signature Files](http://lcl.uniroma1.it/adw/ppvs.30g.1k.tar.gz)
##### Babelfy
- [Babelfy RESTful Java API 1.0](http://babelfy.org/data/BabelfyAPI-1.0.zip)

