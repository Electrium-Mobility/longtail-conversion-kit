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
TaskHandle_t displayToScreen = NULL;
TaskHandle_t telemetryUartComm = NULL;

void app_main() {    
    xTaskCreate(initialize, "initialization", 32768, NULL, 1, &initialization);
    // xTaskCreate(display_to_screen, "Display", 8192, NULL, 1, &displayToScreen);
    // xTaskCreate(fetchValues, "Fetch UART values", 8192, NULL, 1, &telemetryUartComm);
}