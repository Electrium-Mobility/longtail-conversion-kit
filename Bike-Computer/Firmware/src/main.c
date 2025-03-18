#include <stdio.h>
#include <freertos/FreeRTOS.h>
#include <freertos/task.h>
#include <FreeRTOSConfig.h>
#include "freertos/semphr.h"
#include <Arduino.h>

#include "init.h"
#include "ble.h"
#include "display.h"

TaskHandle_t initialization;
TaskHandle_t displayToScreen = NULL;

void app_main() {    
    xTaskCreate(initialize, "initialization", 16384, NULL, 1, &initialization);
    xTaskCreate(display_to_screen, "Display", 4096, NULL, 1, &displayToScreen);
}