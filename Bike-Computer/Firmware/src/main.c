#include <stdio.h>
#include <freertos/FreeRTOS.h>
#include <freertos/task.h>
#include <FreeRTOSConfig.h>
#include "Arduino.h"

#include "ble.h"
#include "display.h"

TaskHandle_t initDisplay = NULL;

void app_main() {
    initArduino();
    
    xTaskCreate(init_display, "Initialize display", 8192, NULL, 5, &initDisplay);
}