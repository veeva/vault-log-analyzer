# Vault Log Analyzer
The Vault Log Analyzer is a command line tool that simplifies the process of analyzing Vault API and Vault Java SDK logs. Through a simple set of commands, the Log Analyzer imports a batch of logs and generates a CSV file of aggregated statistics. The CSV file can then be processed into a pivot table for easy manipulation to provide a holistic view of a Vault's API and SDK performance.

<br />

## Setup

The Vault Log Analyzer tool is distributed as a single JAR file, and does not require installation. Simply navigate to the link below, and click on the "Download" button. From there, the jar file can be run from a command line console.

Download the latest <a href="https://veeva.github.io/vault-log-analyzer/maven/com/veeva/vault/vault-log-analyzer/22.3.0/vault-log-analyzer-22.3.0.jar" download>vault-log-analyzer-22.3.0.jar</a>

We recommend creating a local directory for Vault Log Analyzer related files. This can help with keeping track of the various inputs and outputs.

<br />

## Quick Start

### API Usage Logs

<br />

1. In the same folder where the jar file is, create a folder called "logs"
2. Download the API Usage logs that you want to analyze from Vault, and place them in the "logs" folder 
3. Download the <a href="https://veeva.github.io/vault-log-analyzer/maven/com/veeva/vault/vault-log-analyzer/22.3.0/vault-log-analyzer-22.3.0-api.xlsx" download>vault-log-analyzer-api.xlsx</a>, and place it in the folder with the jar file
4. Open a command line, and navigate to the folder where the jar file is. Run the command below

```
java -jar vault-log-analyzer-22.3.0.jar -logtype API -action IMPORT
```

5. Verify that the "vault-log-analyzer.db" file was generated, then run the following command
```
java -jar vault-log-analyzer-22.3.0.jar -logtype API -action ANALYZE
```

6. Verify that a file named "vault-log-analyzer-api-{YYYYMMDD}-{time}.csv" was generated, then open the "vault-log-analyzer-api.xlsx" file with Excel. From the Ribbon, select **Data -> Refresh All**, and select the generate .csv file

<br />

###  Debug Logs

<br />

1. In the same folder where the jar file is, create a folder called "logs"
2. Download the SDK Debug logs that you want to analyze from Vault, and place them in the "logs" folder 
3. Download the <a href="https://veeva.github.io/vault-log-analyzer/maven/com/veeva/vault/vault-log-analyzer/22.3.0/vault-log-analyzer-22.3.0-debug.xlsx" download>vault-log-analyzer-debug.xlsx</a>, and place it in the folder with the jar file
4. Open a command line, and navigate to the folder where the jar file is. Run the command below

```
java -jar vault-log-analyzer-22.3.0.jar -logtype DEBUG -action ANALYZE
```

5. Verify that a file named "vault-log-analyzer-debug-{YYYYMMDD}-{time}.csv" was generated, then open the "vault-log-analyzer-debug.xlsx" file with Excel. From the Ribbon, select **Data -> Refresh All**, then select the generated .csv file

<br />

## Command Line

The basic structure of a command using Vault Log Analyzer:

```
java -jar {jarFile} -logtype {logType} -action {actionName} -input {folderPath} -output {filePath}
```

<br />

### Commands For API Usage Logs

| Command | Parameter | Example | Description                                                                                                                                                 |
| --- | --- | --- |-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| -logtype | API  | ```-logtype API``` | Set the analyzer to process API Logs                                                                                                                        |
| -action | IMPORT  | ```-action IMPORT``` | Used to import a batch of API logs                                                                                                                          |
|  | ANALYZE  | ```-action ANALYZE``` | Used to analyze generated .db file of ```-action IMPORT``` command                                                                                          |
| -input | {logs folder}  | ```-input ./logs``` | Path to location of log files when using the ```-action IMPORT``` command. Defaults to "/logs" if no directory is given                                     |
|  | {.db file}  | ```-input ./vault-log-analyzer.db``` | Path to location of .db file when using the ```-action ANALYZE``` command                                                                                   |
| -output | {.db file}  | ```-output ./path/to/analyze.db``` | Path for generated .db file when using the ```-action IMPORT``` command. Defaults to "vault-log-analyzer.db" if no filename is given                        |
|  | {.csv file}  | ```-output ./path/to/analyze.db``` | Path for generated .csv file when using the ```-action ANALYZE``` command. Defaults to "vault-log-analyzer-api-YYYYMMDD-{time}.csv" if no filename is given |

<br />

#### Example Commands

1. Import logs
```
java -jar vault-log-analyzer-22.3.0.jar -logtype API -action IMPORT -input ./path/to/logs -output ./path/to/analyze.db
```

2. Analyze .db file
```
java -jar vault-log-analyzer-22.3.0.jar -logtype API -action ANALYZE -input ./path/to/analyze.db -output ./path/to/output.csv
```

<br />

### Commands For SDK Debug Logs

| Command | Parameter     | Example | Description                                                                                                                                                   |
| --- |---------------| --- |---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| -logtype | DEBUG         | ```-logtype DEBUG``` | Set the analyzer to process SDK Debug Logs                                                                                                                    |
| -action | ANALYZE       | ```-action ANALYZE``` | Used to analyze contents of "/logs" folder                                                                                                                    |
| -input | {logs folder} | ```-input ./path/to/logs``` | Path to location of log files. Defaults to "/logs" if no directory is given                                                                                   |
| -output | {.csv file}   | ```-output ./path/to/output.csv``` | Path for generated .csv file when using the ```-action ANALYZE``` command. Defaults to "vault-log-analyzer-debug-YYYYMMDD-{time}.csv" if no filename is given |

#### Example Commands

* Import logs
```
java -jar vault-log-analyzer-22.3.0.jar -logtype DEBUG -action ANALYZE -input ./path/to/logs -output ./path/to/output.csv
```

