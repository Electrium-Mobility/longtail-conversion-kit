#ifndef DISPLAY_H
#define DISPLAY_H

#ifdef __cplusplus
#include "Arduino_GFX_Library.h"
extern "C" {
#endif

#include <stdbool.h>
#include "data.h"

#define TAG "DISPLAY"

//Display specific configuration
#define DISP_WIDTH 480
#define DISP_HEIGHT 272

#define BG_COLOR 0xCC8C
#define MAPS_BG_COLOR 0x1181

//ESP SPI pin configuration
#define PIN_LCD_CS 45     // Chip select
#define PIN_LCD_SCLK 47   // Clock
#define PIN_LCD_MOSI 21   // D0 - MOSI
#define PIN_LCD_MISO 48   // D1 - MISO
#define PIN_LCD_QUADWP 40 // D2 - Reset pin
#define PIN_LCD_QUADHD 39 // D3
#define PIN_BACKLIGHT GPIO_NUM_1

#define WHEEL_DIAMETER 0.7

#define DISPLAY_TAG "DISPLAY"

extern TaskHandle_t displayToScreen;
extern SemaphoreHandle_t displayInitSemaphore;

extern Data etaRelative, etaAbsolute, etaDistance, direction, distanceToNextDirection;
// extern char* bitmap;
extern bool is_connected;

extern int rpm, speed, elevation;

void init_display();

void display_to_screen();

void displayLargeTextMeasurement(const char* title, uint16_t value, bool isSpeed, int16_t startX, int16_t startY);
void displaySmallTextMeasurement(const char* title, uint16_t value, bool isSpeed, int16_t startX, int16_t startY);
void displayMapsDirection();
void animateMapsRectangle(int startYPos, int endYPos, bool show);

#ifdef __cplusplus
}
#endif

#endif