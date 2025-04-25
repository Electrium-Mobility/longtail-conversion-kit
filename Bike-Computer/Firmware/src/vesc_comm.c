#include "vesc_comm.h"
#include "driver/gpio.h"
#include "driver/twai.h"
#include "esp_log.h"
#include "VescCAN.h"
#include <string.h>

dataPackage data;
int rpm;
int speed;

void initCAN() {
    printf("INIT CAN FUNCTION STARTED\n");
    // Initialize configuration structures using macro initializers
    twai_general_config_t g_config = TWAI_GENERAL_CONFIG_DEFAULT(TWAI_TX_PIN, TWAI_RX_PIN, TWAI_MODE_NORMAL);
    twai_timing_config_t t_config = TWAI_TIMING_CONFIG_500KBITS();
    twai_filter_config_t f_config = TWAI_FILTER_CONFIG_ACCEPT_ALL();

    // Install TWAI driver
    if (twai_driver_install(&g_config, &t_config, &f_config) == ESP_OK) {
        ESP_LOGI(VESC_COMM_TAG, "Driver installed");
    } else {
        ESP_LOGI(VESC_COMM_TAG, "Failed to install driver");
        return;
    }

    // Start TWAI driver
    if (twai_start() == ESP_OK) {
        ESP_LOGI(VESC_COMM_TAG, "Driver started");
    } else {
        ESP_LOGI(VESC_COMM_TAG, "Failed to start driver");
        return;
    }

    if (vescComm != NULL) {
        xTaskNotifyGive(vescComm);
    }
}

void fetchVesc() {
    // Wait as long as necessary for initialization to complete
    ESP_LOGI(VESC_COMM_TAG, "Waiting for initialization");
    ulTaskNotifyTake(pdTRUE, portMAX_DELAY);
    ESP_LOGI(VESC_COMM_TAG, "Initialization complete");

    while (1) {
        getVescValues(&data);
        ESP_LOGI(VESC_COMM_TAG, "Input voltage: %.2f, RPM: %.2f", data.inpVoltage, data.rpm);
        rpm = (int)(data.rpm / NUM_POLE_PAIRS);
        speed = (int)((rpm * WHEEL_CIRCUMFERENCE * 60) / 1000.0);
        vTaskDelay(pdMS_TO_TICKS(1000));
    }
}