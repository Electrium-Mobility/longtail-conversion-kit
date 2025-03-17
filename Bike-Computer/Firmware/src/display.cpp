#include "Arduino.h"
#include "Arduino_GFX_Library.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_log.h"
#include "driver/gpio.h"

#include "display.h"

Arduino_GFX *gfx = NULL;

extern "C" void init_display()
{
    // Configure display
    Arduino_DataBus *bus = new Arduino_ESP32QSPI(PIN_LCD_CS, PIN_LCD_SCLK, PIN_LCD_MOSI, PIN_LCD_MISO, PIN_LCD_QUADWP, PIN_LCD_QUADHD);
    Arduino_GFX *g = new Arduino_NV3041A(bus, GFX_NOT_DEFINED, 0, true);
    gfx = new Arduino_Canvas(DISP_WIDTH, DISP_HEIGHT, g);
    
    if (!gfx->begin())
    {
        ESP_LOGI(DISPLAY_TAG, "Display initialization failed");
    }
    else
    {
        ESP_LOGI(DISPLAY_TAG, "Display initialized successfully");
    }
    
    gpio_set_direction(PIN_BACKLIGHT, GPIO_MODE_OUTPUT);
    gpio_set_level(PIN_BACKLIGHT, 1);
    
    gfx->fillScreen(0xC636);
    gfx->setCursor(10, 10);
    gfx->setTextColor(BLACK);
    gfx->println("Hello World!");
    gfx->flush();
    
    ESP_LOGI(DISPLAY_TAG, "Display should be showing content now");

    vTaskDelay(pdMS_TO_TICKS(10000));
}