To install scala
use the instructions https://www.scala-lang.org/download/?_ga=2.136603952.1860049762.1669504024-1343269816.1668201138
curl -fL https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz | gzip -d > cs && chmod +x cs && ./cs setup

install SDKMAN!
curl -s "https://get.sdkman.io" | bash
install sbt
sdk install sbt

run the program from the root folder with command sbt "run {filename}"
Files used should be in the tests folder.
