#ifndef VESC_COMM_H
#define VESC_COMM_H

#include "driver/gpio.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"

#define VESC_COMM_TAG "VESC-COMM"

#define TWAI_TX_PIN GPIO_NUM_17
#define TWAI_RX_PIN GPIO_NUM_18

#define NUM_POLE_PAIRS 7.0 //14 poles
#define WHEEL_CIRCUMFERENCE 1.925 //meters

extern TaskHandle_t vescComm;

void initCAN();
void fetchVesc();

#endif