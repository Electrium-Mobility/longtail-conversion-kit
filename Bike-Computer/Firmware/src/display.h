#ifndef DISPLAY_H
#define DISPLAY_H

#ifdef __cplusplus
#include "Arduino_GFX_Library.h"
extern "C" {
#endif

#include <stdbool.h>

#define TAG "DISPLAY"

//Display specific configuration
#define DISP_WIDTH 480
#define DISP_HEIGHT 272

//ESP SPI pin configuration
#define PIN_LCD_CS 45     // Chip select
#define PIN_LCD_SCLK 47   // Clock
#define PIN_LCD_MOSI 21   // D0 - MOSI
#define PIN_LCD_MISO 48   // D1 - MISO
#define PIN_LCD_QUADWP 40 // D2 - Reset pin
#define PIN_LCD_QUADHD 39 // D3
#define PIN_BACKLIGHT GPIO_NUM_1

#define DISPLAY_TAG "DISPLAY"

void init_display();

#ifdef __cplusplus
}
#endif

#endif