# Vault Log Analyzer
The Vault Log Analyzer is a command line tool that simplifies the process of analyzing Vault API and Vault Java SDK logs. Through a simple set of commands, the Log Analyzer imports a batch of logs and generates a CSV file of aggregated statistics. The CSV file can then be processed into a pivot table for easy manipulation to provide a holistic view of a Vault's API and SDK performance.

## Setup

The Vault Log Analyzer tool is distributed as a single JAR file, and does not require installation. Simply navigate to the link below, and click on the "Download" button. From there, the jar file can be run from a command line console.

Download the latest <a href="https://veeva.github.io/vault-log-analyzer/maven/com/veeva/vault/vault-log-analyzer/24.1.1/vault-log-analyzer-24.1.1.jar" download>vault-log-analyzer-24.1.1.jar</a>

We recommend creating a local directory for Vault Log Analyzer related files. This can help with keeping track of the various inputs and outputs.

## Quick Start

### API Usage Logs

1. In the same folder where the jar file is, create a folder called "logs"
2. Download the API Usage logs that you want to analyze from Vault, and place them in the "logs" folder 
3. Download the <a href="https://veeva.github.io/vault-log-analyzer/maven/com/veeva/vault/vault-log-analyzer/24.1.1/vault-log-analyzer-24.1.1-api.xlsx" download>vault-log-analyzer-api.xlsx</a> file, and place it in the folder with the jar file
4. Open a command line, and navigate to the folder where the jar file is. Run the command below

```
java -jar vault-log-analyzer-24.1.1.jar -logtype API -action IMPORT
```

5. Verify that the "vault-log-analyzer.db" file was generated, then run the following command
```
java -jar vault-log-analyzer-24.1.1.jar -logtype API -action ANALYZE
```

6. Verify that a file named "vault-log-analyzer-api-{YYYYMMDD}-{time}.csv" was generated, then open the "vault-log-analyzer-api.xlsx" file with Excel. From the Ribbon, select **Data -> Refresh All**, and select the generated .csv file

###  Debug Logs


1. In the same folder where the jar file is, create a folder called "logs"
2. Download the SDK Debug logs that you want to analyze from Vault, and place them in the "logs" folder 
3. Download the <a href="https://veeva.github.io/vault-log-analyzer/maven/com/veeva/vault/vault-log-analyzer/24.1.1/vault-log-analyzer-24.1.1-debug.xlsx" download>vault-log-analyzer-debug.xlsx</a> file, and place it in the folder with the jar file
4. Open a command line, and navigate to the folder where the jar file is. Run the command below

```
java -jar vault-log-analyzer-24.1.1.jar -logtype DEBUG -action ANALYZE
```

5. Verify that a file named "vault-log-analyzer-debug-{YYYYMMDD}-{time}.csv" was generated, then open the "vault-log-analyzer-debug.xlsx" file with Excel. From the Ribbon, select **Data -> Refresh All**, then select the generated .csv file

## Command Line

The basic structure of a command using Vault Log Analyzer:

```
java -jar {jarFile} -logtype {logType} -action {actionName} -input {folderPath} -output {filePath}
```


### Commands For API Usage Logs

| Command | Parameter | Example | Description                                                                                                                                                 |
| --- | --- | --- |-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| -logtype | API  | ```-logtype API``` | Set the analyzer to process API Logs                                                                                                                        |
| -action | IMPORT  | ```-action IMPORT``` | Used to import a batch of API logs                                                                                                                          |
|  | ANALYZE  | ```-action ANALYZE``` | Used to analyze generated .db file of ```-action IMPORT``` command                                                                                          |
|  | DOWNLOAD  | ```-action DOWNLOAD``` | Used to download API usage logs from a specified Vault                                                                                          |
| -input | {logs folder}  | ```-input ./logs``` | Optional path to location of log files when using the ```-action IMPORT``` command. Defaults to "/logs" if no directory is given                                     |
|  | {.db file}  | ```-input ./vault-log-analyzer.db``` | Optional path to location of .db file when using the ```-action ANALYZE``` command. Defaults to "vault-log-analyzer.db" in the folder where the jar file      |
| -output | {.db file}  | ```-output ./path/to/analyze.db``` | Optional path for generated .db file when using the ```-action IMPORT``` command. Defaults to "vault-log-analyzer.db" in the folder where the jar file   |
|  | {.csv file}  | ```-output ./path/to/analyze.csv``` | Optional path for generated .csv file when using the ```-action ANALYZE``` command. Defaults to "vault-log-analyzer-api-YYYYMMDD-{time}.csv" if no filename |
|  | {folder}  | ```-output ./path/to/logs``` | Optional path to save API usage logs when using the  ```-action DOWNLOAD``` command. Defaults to "/logs" folder if no directory is given |
| -vaultid | {vault ID}  | ```-vaultid 123456``` | Optional parameter for populating the "vault_id" column in the exported .csv file when using the  ```-action IMPORT/-action ANALYZE``` commands. Defaults to the Vault ID in the api log file name.   |
| -vaultDns | {vault DNS}  | ```-vaultDns cholecap.veevavault.com``` | Vault where logs will be downloaded from when using the  ```-action DOWNLOAD``` command                       |
| -sessionId | {session ID}  | ```-sessionId {session ID}``` | For Authenticating to a Vault when using the  ```-action DOWNLOAD``` command                        |
| -startDate | {YYYY-MM-DD}  | ```-startDate 2023-02-04``` | For specifying a start date when using the  ```-action DOWNLOAD``` command                        |
| -endDate | {YYYY-MM-DD}  | ```-endDate 2023-02-05``` | For specifying an end date when using the  ```-action DOWNLOAD``` command. Start date is required if using an end date. Defaults to the current day if no end date is provided                        |


#### Example Commands

1. Import logs
```
java -jar vault-log-analyzer-24.1.1.jar -logtype API -action IMPORT -input ./path/to/logs -output ./path/to/analyze.db
```

2. Analyze .db file
```
java -jar vault-log-analyzer-24.1.1.jar -logtype API -action ANALYZE -input ./path/to/analyze.db -output ./path/to/output.csv
```

3. Download API Usage Logs
```
java -jar vault-log-analyzer-24.1.1.jar -logtype API -action DOWNLOAD -vaultDns cholecap.veevavault.com -sessionId {session ID} -startDate 2023-02-01 -endDate 2023-02-05
```

### Commands For SDK Debug Logs

| Command | Parameter     | Example | Description                                                                                                                                                   |
| --- |---------------| --- |---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| -logtype | DEBUG         | ```-logtype DEBUG``` | Set the analyzer to process SDK Debug Logs                                                                                                                    |
| -action | ANALYZE       | ```-action ANALYZE``` | Used to analyze contents of "/logs" folder                                                                                                                    |
| -input | {logs folder} | ```-input ./path/to/logs``` | Path to location of log files. Defaults to "/logs" if no directory is given                                                                                   |
| -output | {.csv file}   | ```-output ./path/to/output.csv``` | Path for generated .csv file when using the ```-action ANALYZE``` command. Defaults to "vault-log-analyzer-debug-YYYYMMDD-{time}.csv" if no filename is given |

#### Example Commands

* Analyze logs
```
java -jar vault-log-analyzer-24.1.1.jar -logtype DEBUG -action ANALYZE -input ./path/to/logs -output ./path/to/output.csv
```

### Commands For SDK Runtime Logs

| Command | Parameter | Example | Description                                                                                                                                                 |
| --- | --- | --- |-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| -logtype | RUNTIME  | ```-logtype RUNTIME``` | Set the analyzer to process SDK Runtime Logs                                                                                                                        |
| -action | DOWNLOAD  | ```-action DOWNLOAD``` | Used to download SDK Runtime logs from a specified Vault                                                                                          |
| -vaultDns | {vault DNS}  | ```-vaultDns cholecap.veevavault.com``` | Vault where logs will be downloaded from when using the  ```-action DOWNLOAD``` command                       |
| -sessionId | {session ID}  | ```-sessionId {session ID}``` | For Authenticating to a Vault when using the  ```-action DOWNLOAD``` command                        |
| -startDate | {YYYY-MM-DD}  | ```-startDate 2023-02-04``` | For specifying a start date when using the  ```-action DOWNLOAD``` command                        |
| -endDate | {YYYY-MM-DD}  | ```-endDate 2023-02-05``` | For specifying an end date when using the  ```-action DOWNLOAD``` command. Start date is required if using an end date. Defaults to the current day if no end date is provided                        |

* Download SDK Runtime logs
```
java -jar vault-log-analyzer-24.1.1.jar -logtype RUNTIME -action DOWNLOAD -vaultDns cholecap.veevavault.com -sessionId {session ID} -startDate 2023-02-01 -endDate 2023-02-05
```