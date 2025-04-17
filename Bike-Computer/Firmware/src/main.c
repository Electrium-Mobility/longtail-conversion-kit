#include <stdio.h>
#include <freertos/FreeRTOS.h>
#include <freertos/task.h>
#include <FreeRTOSConfig.h>
#include "freertos/semphr.h"
#include <Arduino.h>

#include "init.h"
#include "ble.h"
#include "display.h"
#include "telemetry.h"

TaskHandle_t initialization;
TaskHandle_t initDisplay;
TaskHandle_t displayToScreen = NULL;
TaskHandle_t telemetryUartComm = NULL;

void app_main() {   
    xTaskCreate(initialize, "Initialize", 65536, NULL, 1, &initialization); 
    xTaskCreatePinnedToCore(display_to_screen, "Display", 4096, NULL, 1, &displayToScreen, 1);
    xTaskCreate(fetchValues, "Fetch UART values", 8192, NULL, 1, &telemetryUartComm);
}