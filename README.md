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
$ java -jar phph-all.jar browse                                  # view patterns at localhost:8080
```

## General Options
- `--log=<level>`: Specify log level; either of `ERROR`, `WARN`, `INFO`, `DEBUG`, `TRACE` (Default: `INFO`)
- `-q`, `--quiet`: quiet mode (same as `--log=ERROR`)
- `-v`, `--verbose`: verbose mode (same as `--log=DEBUG`)
- `--help`: Show the help message and exit
- `--version`: print version information and exit
- `-f`, `--database`: Specifies database file path (Default: `phph.db`)

## Commands

### init
`init` is for initializing the phph database.
phph uses [SQLite](https://www.sqlite.org/) as its databse backend.
This command creates an SQLite database file (default: "phph.db" at the current directory).
```
$ java -jar phph-all.jar init
$ java -jar phph-all.jar --database=path/to/database.db init  # Use a different path for the database
```

### extract
`extract` extracts changes from Git repositories.
```
$ java -jar phph-all.jar extract --repository=path/to/repo/.git
```

Options:
* `-p`, `--parallel=<nthreads>`: Parallel processing.
  The number of threads (<nthreads>) to use in parallel can be specified.
  If `<nthreads>` is omitted, _the total number of processors - 1_ is used. 
* `--from=<rev>`: Revision to skip go further (exclusive).
* `--end=<rev>`: Revision to start traversing (default: `HEAD`)
* `--differencer=<type>`: Specify the differencer used; either of `myers` or `histogram` (Default: `myers`)
* `--splitter=<type>`: Specify the splitter used; either of `plain`, `mpa`, `nitron` (Default: `mpa`)
* `--non-essential`: Use non-essential statements
* `--min-size`: Minimum chunk size (default: `0`)
* `--max-size`: Maximum chunk size (default: `Integer.MAX_VALUE`)

### find
`find` finds pattern application opportinuties

### measure
`measure` computes metric values

### browse
`browse` provides a view of patterns at `localhost:8080`

## Acknowledgments
This tool is inspired by [MPAnalyzer](https://github.com/YoshikiHigo/MPAnalyzer)/[Ammonia](https://github.com/YoshikiHigo/NH3), and its basic design is based on them.

## Trivia
phph is an abbreviation of [Phenolphthalein](https://en.wikipedia.org/wiki/Phenolphthalein).
