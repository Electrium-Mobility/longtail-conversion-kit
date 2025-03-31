#ifndef INIT_H
#define INIT_H

#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "data.h"

#define INIT_TAG "Initialization"

extern SemaphoreHandle_t initMutex;

void initialize();

#endif