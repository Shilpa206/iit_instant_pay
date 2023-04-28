set HOME=%cd%
set RESOURCES_DIR_PATH=%HOME%\resources
set DB_PROPERTIES_FILE=%HOME%\conf\prodDb.properties

java --module-path %HOME%\dependencies\javafx-sdk-17.0.7\lib --add-modules javafx.controls,javafx.fxml -jar %HOME%\iit_instant_pay.jar


