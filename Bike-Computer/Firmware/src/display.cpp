#include "Arduino.h"
#include "Arduino_GFX_Library.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_log.h"
#include "driver/gpio.h"
#include <string.h>
#include <stdint.h>
#include <math.h>

#include "display.h"

#include "icons.h"
#include "fonts/FreeSans10pt7b.h"
#include "fonts/FreeSans14pt7b.h"
#include "fonts/FreeSans24pt7b.h"
#include "fonts/FreeSansBold11pt7b.h"
#include "fonts/FreeSansBold30pt7b.h"
#include "fonts/FreeSansBold60pt7b.h"

Arduino_GFX *gfx = NULL;
bool prevIsConnected = false;

SemaphoreHandle_t initMutex;

extern "C" void init_display()
{

    strcpy(etaRelative, "15 min");
    strcpy(etaDistance, "7km");
    strcpy(etaAbsolute, "10:04PM");
    strcpy(bitmap, "STRAIGHT");
    strcpy(directionInstruction, "Markham road");
    strcpy(distanceToNextDirection, "500m");
    // Configure display
    Arduino_DataBus *bus = new Arduino_ESP32QSPI(PIN_LCD_CS, PIN_LCD_SCLK, PIN_LCD_MOSI, PIN_LCD_MISO, PIN_LCD_QUADWP, PIN_LCD_QUADHD);
    Arduino_GFX *g = new Arduino_NV3041A(bus, GFX_NOT_DEFINED, 0, true);
    gfx = new Arduino_Canvas_Indexed(DISP_WIDTH, DISP_HEIGHT, g, 0, 0, 0, 1);

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

    // Signal to display function that initialization is done
    if (displayToScreen != NULL)
    {
        xTaskNotifyGive(displayToScreen);
    }
}

void parseBLEPayload(Data *data, char *str1, char *str2, char *str3)
{
    char *vals[3] = {str1, str2, str3};   // Store in array for easier access
    const char *iterChar = data->payload; // Pointer to beginning of string
    ESP_LOGI(DISPLAY_TAG, "Data payload: %s", data->payload);
    int index = 0;
    while (index < 3)
    {
        int bufIndex = 0;
        char *end = (index < 2 ? strchr(iterChar, ',') : strchr(iterChar, '\0'));

        // Copy values between commas into strBuf
        char strBuf[32] = "";
        while (iterChar != end)
        {
            strBuf[bufIndex] = *iterChar;
            ++iterChar;
            ++bufIndex;
        }
        strcpy(vals[index], strBuf);
        ESP_LOGI(DISPLAY_TAG, "Index: %d, Text: %s", index, vals[index]);
        ++iterChar;
        ++index;
    }
}

void display_to_screen()
{
    // Wait as long as necessary for initialization to complete
    ESP_LOGI(DISPLAY_TAG, "Waiting for initialization");
    ulTaskNotifyTake(pdTRUE, portMAX_DELAY);
    ESP_LOGI(DISPLAY_TAG, "Initialization complete");

    while (1)
    {
        for (uint16_t i = 2; i < 25; ++i)
        {
            // Update values from BLE write
            if (eta.updated)
            {
                parseBLEPayload(&eta, etaRelative, etaDistance, etaAbsolute);
                eta.updated = false;
            }
            if (direction.updated)
            {
                parseBLEPayload(&direction, bitmap, directionInstruction, distanceToNextDirection);
                direction.updated = false;
            }
            gfx->fillScreen(BG_COLOR);
            
            displayLargeTextMeasurement("SPEED", speed, true, 10, 10);
            displayLargeTextMeasurement("RPM", rpm, false, 270, 10);
            displaySmallTextMeasurement("PAS", (i + 1) / 3, true, 10, 130);
            displaySmallTextMeasurement("ELEVATION", elevation, false, 270, 130);

            displayMapsDirection();
            gfx->flush();

            vTaskDelay(pdMS_TO_TICKS(100));
        }
        vTaskDelay(pdMS_TO_TICKS(1000));
    }
}

void displayLargeTextMeasurement(const char *title, uint16_t value, bool isSpeed, int16_t startX, int16_t startY)
{
    int pixelsForNumber = 70;
    if (value >= 10)
    {
        pixelsForNumber *= 2;
    }

    char valueBuffer[4];
    
    snprintf(valueBuffer, 4 * sizeof(uint16_t), "%u", value);

    gfx->setTextColor(BLACK);

    gfx->setFont(&FreeSans10pt7b);
    gfx->setCursor(startX, startY + 10); // Y position of this function represents the bottom
    gfx->println(title);

    gfx->setFont(&FreeSansBold60pt7b);
    gfx->setCursor(startX, startY + 102);
    gfx->println(valueBuffer);

    if (isSpeed)
    {
        gfx->setFont(&FreeSans24pt7b);
        gfx->setCursor(startX + pixelsForNumber, startY + 51);
        gfx->println("km/");
        gfx->setCursor(startX + pixelsForNumber, startY + 91);
        gfx->println("h");
    }
}

void displaySmallTextMeasurement(const char *title, uint16_t value, bool isSpeed, int16_t startX, int16_t startY)
{
    int pixelsForNumber = 33;
    if (value >= 1000)
    {
        pixelsForNumber *= 4;
    }
    else if (value >= 100)
    {
        pixelsForNumber *= 3;
    }
    else if (value >= 10)
    {
        pixelsForNumber *= 2;
    }

    char valueBuffer[4];
    if (value == UINT16_MAX)
    {
        snprintf(valueBuffer, 4 * sizeof(uint16_t), "-");
        pixelsForNumber = 25;
    }
    else
    {
        snprintf(valueBuffer, 4 * sizeof(uint16_t), "%u", value);
    }

    gfx->setTextColor(BLACK);

    gfx->setFont(&FreeSans10pt7b);
    gfx->setCursor(startX, startY + 10); // Y position of this function represents the bottom
    gfx->println(title);

    gfx->setFont(&FreeSansBold30pt7b);
    gfx->setCursor(startX, startY + 60);
    gfx->println(valueBuffer);

    // PAS
    if (isSpeed)
    {
        gfx->setFont(&FreeSans14pt7b);
        gfx->setCursor(startX + pixelsForNumber, startY + 60);
    }
    else
    { // Elevation
        gfx->setFont(&FreeSans14pt7b);
        gfx->setCursor(startX + pixelsForNumber, startY + 60);
        gfx->println("m");
    }
}

void displayMapsDirection()
{
    // Animate when connection status changes
    if (is_connected && !prevIsConnected)
    {
        animateMapsRectangle(272, 200, true);
        prevIsConnected = true;
    }
    else if (!is_connected && prevIsConnected)
    {
        animateMapsRectangle(272, 200, false);
        prevIsConnected = false;
    }
    // Draw rounded rectangle that covers the bottom part of the screen
    else if (is_connected)
    {
        gfx->fillRoundRect(0, 200, DISP_WIDTH, 72, 10, MAPS_BG_COLOR);
        gfx->fillRect(0, 265, DISP_WIDTH, 7, MAPS_BG_COLOR);

        gfx->drawLine(90, 205, 90, 267, 0x4208);

        gfx->setTextColor(WHITE);

        if (strcmp(bitmap, "TURN_LEFT") == 0)
        {
            gfx->draw16bitRGBBitmapWithTranColor(25, 205, TURN_LEFT, 0x0000, 40, 40);
        }
        else if (strcmp(bitmap, "TURN_RIGHT") == 0)
        {
            gfx->draw16bitRGBBitmapWithTranColor(25, 205, TURN_RIGHT, 0x0000, 40, 40);
        }
        else if (strcmp(bitmap, "SLIGHT_LEFT") == 0)
        {
            gfx->draw16bitRGBBitmapWithTranColor(25, 205, SLIGHT_LEFT, 0x0000, 40, 40);
        }
        else if (strcmp(bitmap, "SLIGHT_RIGHT") == 0)
        {
            gfx->draw16bitRGBBitmapWithTranColor(25, 205, SLIGHT_RIGHT, 0x0000, 40, 40);
        }
        else if (strcmp(bitmap, "SHARP_LEFT") == 0)
        {
            gfx->draw16bitRGBBitmapWithTranColor(25, 205, SHARP_LEFT, 0x0000, 40, 40);
        }
        else if (strcmp(bitmap, "SHARP_RIGHT") == 0)
        {
            gfx->draw16bitRGBBitmapWithTranColor(25, 205, SHARP_RIGHT, 0x0000, 40, 40);
        }
        else if (strcmp(bitmap, "DEST_LEFT") == 0)
        {
            gfx->draw16bitRGBBitmapWithTranColor(25, 205, DEST_LEFT, 0x0000, 40, 40);
        }
        else if (strcmp(bitmap, "DEST_RIGHT") == 0)
        {
            gfx->draw16bitRGBBitmapWithTranColor(25, 205, DEST_RIGHT, 0x0000, 40, 40);
        }
        else
        {
            gfx->draw16bitRGBBitmapWithTranColor(25, 205, STRAIGHT, 0x0000, 40, 40);
        }

        // Center distance to next direction
        int charWidth = 13;
        int textWidth = charWidth * strlen(distanceToNextDirection);
        // Display distance to next direction
        gfx->setCursor((90 - textWidth) / 2, 265); // Window is 90px wide, want to center
        gfx->setFont(&FreeSansBold11pt7b);
        gfx->println(distanceToNextDirection);

        // Display direction
        gfx->setCursor(100, 230);
        gfx->setFont(&FreeSans14pt7b);
        gfx->println(directionInstruction);

        char etaMessage[382];
        sprintf(etaMessage, "ETA: %s - %s - %s", etaRelative, etaAbsolute, etaDistance);
        // Display ETA
        gfx->setCursor(100, 265);
        gfx->setFont(&FreeSans10pt7b);
        gfx->println(etaMessage);
    }
}

void animateMapsRectangle(int startYPos, int endYPos, bool show)
{
    if (show)
    {
        for (int yPos = startYPos; yPos >= endYPos; yPos -= 6)
        {
            gfx->fillRoundRect(0, yPos, DISP_WIDTH, (startYPos - yPos), 10, MAPS_BG_COLOR);
            gfx->fillRect(0, yPos + 7, DISP_WIDTH, 7, MAPS_BG_COLOR);
            gfx->flush();
        }
    }
    else
    {
        for (int yPos = endYPos; yPos <= startYPos; yPos += 6)
        {
            gfx->fillRect(0, endYPos, DISP_WIDTH, (startYPos - endYPos), BG_COLOR);
            gfx->fillRoundRect(0, yPos, DISP_WIDTH, (startYPos - yPos), 10, MAPS_BG_COLOR);
            gfx->fillRect(0, (yPos < (startYPos - 7) ? (startYPos - 7) : startYPos), DISP_WIDTH, 7, MAPS_BG_COLOR);
            gfx->flush();
        }
    }
}