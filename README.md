# phph
A flyweight change pattern extractor and change recommender

[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://github.com/sh5i/git-stein/blob/master/LICENSE)

## Build
```
$ git clone https://github.com/salab/phph.git
$ cd phph
$ ./gradlew shadowJar
$ java -jar build/libs/phph-all.jar <cmd> [options...]
```

## Usage
```
$ java -jar phph-all.jar init                                    # initalize database
$ java -jar phph-all.jar extract --repository=/path/to/repo/.git # extract patterns
$ java -jar phph-all.jar find                                    # find pattern application opportinuties
$ java -jar phph-all.jar measure                                 # compute metric values
```

## Acknowledgments
This tool is inspired by [MPAnalyzer](https://github.com/YoshikiHigo/MPAnalyzer)/[Ammonia](https://github.com/YoshikiHigo/NH3), and its basic design is based on them.
