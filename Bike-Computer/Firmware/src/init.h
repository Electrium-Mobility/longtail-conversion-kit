#ifndef INIT_H
#define INIT_H

#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "data.h"

#define INIT_TAG "Initialization"

extern int rpm;
extern int elevation;

extern Data etaRelative, etaAbsolute, etaDistance, direction, distanceToNextDirection;
extern char* bitmap;

extern SemaphoreHandle_t initMutex;

void initialize();

#endif