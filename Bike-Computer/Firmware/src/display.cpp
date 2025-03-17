#include "Arduino.h"
#include "Arduino_GFX_Library.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_log.h"
#include "driver/gpio.h"

#include "display.h"
#include "fonts/FreeSans10pt7b.h"
#include "fonts/FreeSansBold30pt7b.h"
#include "fonts/FreeSansBold60pt7b.h"
#include "fonts/FreeSans24pt7b.h"
#include "fonts/FreeSans14pt7b.h"

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

    gfx->fillScreen(0xCC8C);

    for (uint16_t i = 2; i < 25; ++i) {
        gfx->fillScreen(0xCC8C);
        displayLargeTextMeasurement("SPEED", i, true, 10, 10);
        displayLargeTextMeasurement("RPM", i + 5, false, 270, 10);
        displaySmallTextMeasurement("PAS", i - 2, true, 10, 130);
        displaySmallTextMeasurement("ELEVATION", i * 100, false, 270, 130);
        gfx->flush();
    
        vTaskDelay(pdMS_TO_TICKS(1000));
    }
}

void displayLargeTextMeasurement(const char* title, uint16_t value, bool isSpeed, int16_t startX, int16_t startY) {
    int pixelsForNumber = 70;
    if (value >= 10) {
        pixelsForNumber *= 2;
    }

    char valueBuffer[4];
    snprintf(valueBuffer, 4 * sizeof(uint16_t), "%u", value);

    gfx->setTextColor(BLACK);

    gfx->setFont(&FreeSans10pt7b);
    gfx->setCursor(startX, startY + 10); //Y position of this function represents the bottom
    gfx->println(title);

    gfx->setFont(&FreeSansBold60pt7b);
    gfx->setCursor(startX, startY + 102);
    gfx->println(valueBuffer);

    if (isSpeed) {
        gfx->setFont(&FreeSans24pt7b);
        gfx->setCursor(startX + pixelsForNumber, startY + 51);
        gfx->println("km/");
        gfx->setCursor(startX + pixelsForNumber, startY + 91);
        gfx->println("h");
    }
}

void displaySmallTextMeasurement(const char* title, uint16_t value, bool isSpeed, int16_t startX, int16_t startY) {
    int pixelsForNumber = 33;
    if (value >= 1000) {
        pixelsForNumber *= 4;
    }
    else if (value >= 100) {
        pixelsForNumber *= 3;
    }
    else if (value >= 10) {
        pixelsForNumber *= 2;
    }

    char valueBuffer[4];
    snprintf(valueBuffer, 4 * sizeof(uint16_t), "%u", value);

    gfx->setTextColor(BLACK);

    gfx->setFont(&FreeSans10pt7b);
    gfx->setCursor(startX, startY + 10); //Y position of this function represents the bottom
    gfx->println(title);

    gfx->setFont(&FreeSansBold30pt7b);
    gfx->setCursor(startX, startY + 60);
    gfx->println(valueBuffer);

    //PAS
    if (isSpeed) {
        gfx->setFont(&FreeSans14pt7b);
        gfx->setCursor(startX + pixelsForNumber, startY + 60);
        gfx->println("km/h");
    }
    else { //Elevation
        gfx->setFont(&FreeSans14pt7b);
        gfx->setCursor(startX + pixelsForNumber, startY + 60);
        gfx->println("m");
    }
}