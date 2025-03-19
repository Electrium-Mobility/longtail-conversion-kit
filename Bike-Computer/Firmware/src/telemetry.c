#include "driver/gpio.h"
#include "driver/uart.h"

#include "VescUart.h"
#include "telemetry.h"

int rpm;
int elevation;
dataPackage *vescData;

void initUart() {
    uart_set_pin(VESC_UART_NUM, GPIO_NUM_0, GPIO_NUM_1, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE);

    //Initialize UART
    uart_config_t uart_config = {
		.baud_rate = 115200,
		.data_bits = UART_DATA_8_BITS,
		.parity = UART_PARITY_DISABLE,
		.stop_bits = UART_STOP_BITS_1,
		.flow_ctrl = UART_HW_FLOWCTRL_DISABLE,
		.rx_flow_ctrl_thresh = 122,
	};

	ESP_ERROR_CHECK(uart_param_config(VESC_UART_NUM, &uart_config));
	ESP_ERROR_CHECK(uart_set_pin(VESC_UART_NUM, 0, 1, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE));
	ESP_ERROR_CHECK(uart_driver_install(VESC_UART_NUM, 256, 256, 0, NULL, 0));

    ESP_ERROR_CHECK(uart_param_config(GPS_UART_NUM, &uart_config));
	ESP_ERROR_CHECK(uart_set_pin(GPS_UART_NUM, 0, 1, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE));
	ESP_ERROR_CHECK(uart_driver_install(GPS_UART_NUM, 256, 256, 0, NULL, 0));
}

void readElevation() {

}

void fetchVesc() {
    while (1) {
        vTaskDelay(100 / portTICK_PERIOD_MS);
        getVescValues(vescData);
        rpm = vescData->rpm;
    }
}