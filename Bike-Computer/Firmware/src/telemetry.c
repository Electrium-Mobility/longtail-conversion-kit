#include "driver/gpio.h"
#include "driver/uart.h"
#include "esp_log.h"
#include <string.h>
#include <stdlib.h>
#include <math.h>

#include "VescUart.h"
#include "telemetry.h"

int rpm;
int speed;
int elevation;

dataPackage *vescData;

void initUart() {
	//Set up GPS module UART (UART1)
    uart_config_t gps_uart_config = {
		.baud_rate = 9600,
		.data_bits = UART_DATA_8_BITS,
		.parity = UART_PARITY_DISABLE,
		.stop_bits = UART_STOP_BITS_1,
		.flow_ctrl = UART_HW_FLOWCTRL_DISABLE
	};

	ESP_ERROR_CHECK(uart_param_config(GPS_UART_NUM, &gps_uart_config));
	ESP_ERROR_CHECK(uart_set_pin(GPS_UART_NUM, 17, 18, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE));
	ESP_ERROR_CHECK(uart_driver_install(GPS_UART_NUM, GPS_BUFFER_SIZE, 0, 0, NULL, 0));

	//Set up VESC UART (UART2)
	uart_config_t vesc_uart_config = {
		.baud_rate = 115200,
		.data_bits = UART_DATA_8_BITS,
		.parity = UART_PARITY_DISABLE,
		.stop_bits = UART_STOP_BITS_1,
		.flow_ctrl = UART_HW_FLOWCTRL_DISABLE,
		.rx_flow_ctrl_thresh = 122,
	};

	ESP_ERROR_CHECK(uart_param_config(VESC_UART_NUM, &vesc_uart_config));
	ESP_ERROR_CHECK(uart_set_pin(VESC_UART_NUM, 0, 1, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE));
	ESP_ERROR_CHECK(uart_driver_install(VESC_UART_NUM, 256, 256, 0, NULL, 0));

	if (telemetryUartComm != NULL) {
        xTaskNotifyGive(telemetryUartComm);
    }
}

void rawReadings(char* buf) {
    memset(buf, 0, GPS_BUFFER_SIZE);  // Clear the buffer
    int len = uart_read_bytes(GPS_UART_NUM, buf, GPS_BUFFER_SIZE - 1, portMAX_DELAY);  // Read data
    if (len > 0) {
        buf[len] = 0;  // Ensure null termination
        
        // Check if $GPVTG or $GPGGA exists in the buffer
        if (strstr(buf, "$GPVTG") != NULL) {
            ESP_LOGI(TELEMETRY_TAG, "Found $GPVTG: %s", buf);
        }
        else if (strstr(buf, "$GPGGA") != NULL) {
            ESP_LOGI(TELEMETRY_TAG, "Found $GPGGA: %s", buf);
        }
    } else {
        ESP_LOGI(TELEMETRY_TAG, "No data received within timeout");
    }
}

void parseNMEA(const char *subfield, float *attribute, int index) {
    if (subfield != NULL) {
        const char *iterChar = subfield;
        int numCommas = 0;
        while (iterChar != NULL && *iterChar != '\0') {
            if (*iterChar == ',') {
                ++numCommas;
            }
            ++iterChar;
            if (numCommas == (index - 1)) {
                break;
            }
        }

        //Edge case where there's not enough fields
        if (numCommas < (index - 1)) {
            *attribute = INFINITY;
            return;
        }
        char* end = strchr(iterChar, ',');
        if (end == NULL) {
            *attribute = INFINITY;
            return;
        }

        //Iterate between the commas and store this in strBuf
        char strBuf[10];
        int bufIndex = 0;
        while (iterChar != NULL && *iterChar != '\0') {
            if (iterChar == end) {
                break;
            }
            strBuf[bufIndex] = *iterChar;
            ++bufIndex;
            ++iterChar;
        }

        //Target field looks like ,, where it's null
        if (bufIndex == 0) {
            *attribute = INFINITY;
            return;
        }

        *attribute = atof(strBuf);
    }
    else {
        *attribute = INFINITY;
    }
}

void readElevationAndSpeed(const char *buf) {
	//Temp variables to assign to global elevation/speed
	float altitude;
	float velocity;

    //Assign pointers to the lines we want to extract from
    const char *gpgga = strstr(buf, "$GPGGA");
    const char *gpvtg = strstr(buf, "$GPVTG");

	//Copy NMEA values to temp variables
    parseNMEA(gpgga, &altitude, 10);
    parseNMEA(gpvtg, &velocity, 8);

	//Assign to global variables
	if (isinf((double)altitude)) {
		elevation = INT_MAX;
	}
	else {
		elevation = (int)altitude;
	}
	if (isinf((double)velocity)) {
		speed = INT_MAX;
	}
	else {
		speed = (int)velocity;
	}
}

void fetchValues() {
	    //Wait as long as necessary for initialization to complete
	ESP_LOGI(TELEMETRY_TAG, "Waiting for initialization");
	ulTaskNotifyTake(pdTRUE, portMAX_DELAY);
	ESP_LOGI(TELEMETRY_TAG, "Initialization complete");

	static char buf[GPS_BUFFER_SIZE]; 
    while (1) {
        vTaskDelay(100 / portTICK_PERIOD_MS);
		//Fetch VESC values and update rpm
        getVescValues(vescData);
        rpm = vescData->rpm;

		//Fetch GPS values and update elevation and speed
		rawReadings(buf);
		readElevationAndSpeed(buf);
    }
}