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
#include "vesc_comm.h"

TaskHandle_t initialization;
TaskHandle_t initDisplay;
TaskHandle_t displayToScreen = NULL;
TaskHandle_t telemetryUartComm = NULL;
TaskHandle_t vescComm = NULL;

void app_main() {   
    xTaskCreatePinnedToCore(display_to_screen, "Display", 4096, NULL, 1, &displayToScreen, 1);
    xTaskCreate(fetchValues, "Fetch UART values", 8192, NULL, 1, &telemetryUartComm);
    xTaskCreate(fetchVesc, "Fetch VESC values", 8192, NULL, 1, &vescComm);
    xTaskCreate(initialize, "Initialize", 65536, NULL, 1, &initialization); 
}