#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include <driver/uart.h>
#include <driver/twai.h>
#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include "VescCAN.h"

bool sendMessage(uint8_t controllerId, CAN_PACKET_ID cmd, uint8_t *data, uint8_t len) {
    if (len > 8) {
        ESP_LOGE(VESC_CAN_TAG, "CAN message too long: %d bytes", len);
        return false;
    }
    twai_message_t message;
    message.identifier = ((uint32_t)controllerId << 8) | cmd;
    message.data_length_code = len;
    message.flags = TWAI_MSG_FLAG_EXTD;

    memcpy(message.data, data, len);

    esp_err_t result = twai_transmit(&message, portMAX_DELAY);
    if (result != ESP_OK) {
        ESP_LOGE(VESC_CAN_TAG, "Failed to send CAN message: %d", result);
        return false;
    }

    return true;
}

bool receiveMessage(uint8_t *controllerId, CAN_PACKET_ID *cmd, uint8_t *data, uint8_t *len) {
    twai_message_t message;
    esp_err_t result = twai_receive(&message, portMAX_DELAY);
    if (result != ESP_OK) {
        ESP_LOGE(VESC_CAN_TAG, "Failed to receive CAN message: %s", esp_err_to_name(result));
        return false;
    }

    *cmd = (CAN_PACKET_ID)(message.identifier >> 8) & 0xFF;
    *controllerId = (message.identifier & 0xFF);

    //ESP_LOGI(VESC_CAN_TAG, "Controller ID: %d, Command: %d", *controllerId, *cmd);

    *len = message.data_length_code;
    memcpy(data, message.data, *len);
    
    return true;
}

bool getVescValues(dataPackage* data) {
	return getVescValuesCAN(data, ESC_ID);
}

bool getVescValuesCAN(dataPackage *data, uint8_t canId) {
    //ESP_LOGI(VESC_CAN_TAG, "Getting VESC values");

    uint8_t controllerId;
    CAN_PACKET_ID cmd;
    uint8_t rx_data[8];
    uint8_t len;
	
    int received_frames = 0;
    uint32_t timeout = xTaskGetTickCount() + pdMS_TO_TICKS(1000);

    while (received_frames < 4 && xTaskGetTickCount() < timeout) {
        if (receiveMessage(&controllerId, &cmd, rx_data, &len)) {
            if (controllerId == canId) {
                int32_t index = 0;

                switch (cmd) {
                    case CAN_PACKET_STATUS:
                        data->rpm = (float)buffer_get_int32(rx_data, &index);
                        data->avgMotorCurrent = (float)buffer_get_int16(rx_data, &index) / 10.0;
                        data->dutyCycleNow = (float)buffer_get_int16(rx_data, &index) / 1000.0;
                        //ESP_LOGI(VESC_CAN_TAG, "Retrieved data - RPM: %.2f, Avg motor current: %.2f, Duty cycle now: %.2f", data->rpm, data->avgMotorCurrent, data->dutyCycleNow);
                        received_frames++;
                        break;
                    
                    case CAN_PACKET_STATUS_2:
                        data->ampHours = (float)buffer_get_int32(rx_data, &index) / 10000.0;
                        data->ampHoursCharged = (float)buffer_get_int32(rx_data, &index) / 10000.0;
                        //ESP_LOGI(VESC_CAN_TAG, "Retrieved data - Amp hours: %.2f, Amp hours charged: %.2f", data->ampHours, data->ampHoursCharged);
                        received_frames++;
                        break;
                    
                    case CAN_PACKET_STATUS_3:
                        data->wattHours = (float)buffer_get_int32(rx_data, &index) / 10000.0;
                        data->wattHoursCharged = (float)buffer_get_int32(rx_data, &index) / 10000.0;
                        //ESP_LOGI(VESC_CAN_TAG, "Retrieved data - Watt hours: %.2f, Watt hours charged: %.2f", data->wattHours, data->wattHoursCharged);
                        received_frames++;
                        break;
                    
                    case CAN_PACKET_STATUS_4:
                        data->tempMosfet = (float)buffer_get_int16(rx_data, &index) / 10.0;
                        data->tempMotor = (float)buffer_get_int16(rx_data, &index) / 10.0;
                        data->inpVoltage = (float)buffer_get_int16(rx_data, &index) / 10.0;
                        data->error = rx_data[6];
                        //ESP_LOGI(VESC_CAN_TAG, "Retrieved data - Temp mosfet: %.2f, Temp motor: %.2f, Input voltage: %.2f", data->tempMosfet, data->tempMotor, data->inpVoltage);
                        received_frames++;
                        break;
                    
                    default:
                        break;
                }
            }
        }
    }

    return (received_frames > 0);
}

void setCurrent(dataPackage *data, float current) {
    return setCurrentCAN(data, current, 0);
}

void setCurrentCAN(dataPackage *data, float current, uint8_t canId) {
    uint8_t buffer[4];
    int32_t index = 0;
    
    buffer_append_int32(buffer, (int32_t)(current * 1000), &index);
    
    sendMessage(canId, CAN_PACKET_SET_CURRENT, buffer, 4);
}

void setRPM(dataPackage *data, float rpm) {
    return setRPMCAN(data, rpm, 0);
}

void setRPMCAN(dataPackage *data, float rpm, uint8_t canId) {
    uint8_t buffer[4];
    int32_t index = 0;
    
    buffer_append_int32(buffer, (int32_t)(rpm), &index);
    
    sendMessage(canId, CAN_PACKET_SET_RPM, buffer, 4);
}

void setDuty(dataPackage *data, float duty) {
    return setDutyCAN(data, duty, 0);
}

void setDutyCAN(dataPackage *data, float duty, uint8_t canId) {
    uint8_t buffer[4];
    int32_t index = 0;
    
    buffer_append_int32(buffer, (int32_t)(duty * 100000), &index);
    
    sendMessage(canId, CAN_PACKET_SET_DUTY, buffer, 4);
}

void setBrakeCurrent(dataPackage *data, float brakeCurrent) {
    return setBrakeCurrentCAN(data, brakeCurrent, 0);
}

void setBrakeCurrentCAN(dataPackage *data, float brakeCurrent, uint8_t canId) {
    uint8_t buffer[4];
    int32_t index = 0;
    
    buffer_append_int32(buffer, (int32_t)(brakeCurrent * 1000), &index);
    
    sendMessage(canId, CAN_PACKET_SET_CURRENT_BRAKE, buffer, 4);
}