#include <stdint.h>
#include <stdbool.h>
#include <string.h>
#include <driver/uart.h>
#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include "VescUart.h"

// Timeout - specifies how long the function will wait for the vesc to respond
const uint32_t _TIMEOUT = 100;

/** Variabel to hold nunchuck values */
nunchuckPackage nunchuck;

/** Variable to hold firmware version */
FWversionPackage fw_version;

uint32_t serialAvailable(uart_port_t uart_num) {
	size_t availableBytes = 0;
	if (uart_is_driver_installed(uart_num)) {
		uart_get_buffered_data_len(uart_num, &availableBytes);
		printf("Buffered length: %d\n", availableBytes);
	} else {
		printf("UART driver is not installed!");
	}
    return (uint32_t) availableBytes;
}

int32_t receiveUartMessage(dataPackage *data, uint8_t * payloadReceived) {
	printf("Receiving");

	// Messages <= 255 starts with "2", 2nd byte is length
	// Messages > 255 starts with "3" 2nd and 3rd byte is length combined with 1st >>8 and then &0xFF

	uint16_t counter = 0;
	uint16_t endMessage = 256;
	bool messageRead = false;
	uint8_t messageReceived[256];
	uint16_t lenPayload = 0;
	
	uint32_t timeout = xTaskGetTickCount() + (pdMS_TO_TICKS(_TIMEOUT)); // Defining the timestamp for timeout (100ms before timeout)

	while (xTaskGetTickCount() < timeout && messageRead == false) {

		while (serialAvailable(UART_NUM) > 0) {
			uint8_t byte;
			int bytes_read = uart_read_bytes(UART_NUM, &byte, 1, pdMS_TO_TICKS(10));
			if (bytes_read == 1) {
				messageReceived[counter++] = byte;
			}

			if (counter == 2) {

				switch (messageReceived[0])
				{
					case 2:
						endMessage = messageReceived[1] + 5; //Payload size + 2 for sice + 3 for SRC and End.
						lenPayload = messageReceived[1];
					break;

					case 3:
						// ToDo: Add Message Handling > 255 (starting with 3)
						printf("Message is larger than 256 bytes - not supported");
					break;

					default:
						printf("Invalid start bit");
					break;
				}
			}

			if (counter >= sizeof(messageReceived)) {
				break;
			}

			if (counter == endMessage && messageReceived[endMessage - 1] == 3) {
				messageReceived[endMessage] = 0;
				printf("End of message reached");
				messageRead = true;
				break; // Exit if end of message is reached, even if there is still more data in the buffer.
			}
		}
	}

	if(messageRead == false) {
		printf("Timeout");
	}
	
	bool unpacked = false;

	if (messageRead) {
		unpacked = unpackPayload(messageReceived, endMessage, payloadReceived);
	}

	if (unpacked) {
		// Message was read
		return lenPayload; 
	}
	else {
		// No Message Read
		printf("No message read");
		return 0;
	}
}


bool unpackPayload(uint8_t * message, int32_t lenMes, uint8_t * payload) {

	uint16_t crcMessage = 0;
	uint16_t crcPayload = 0;

	// Rebuild crc:
	crcMessage = message[lenMes - 3] << 8;
	crcMessage &= 0xFF00;
	crcMessage += message[lenMes - 2];

	printf("SRC received: %hu", crcMessage);

	// Extract payload:
	memcpy(payload, &message[2], message[1]);

	crcPayload = crc16(payload, message[1]);

	printf("SRC calc: %hu", crcPayload);
	
	// Debug log bytes in hex format
    printf("Message header: %02x %02x", message[0], message[1]);
    printf("Payload first few bytes: %02x %02x %02x %02x", 
             payload[0], payload[1], payload[2], payload[3]);
	
	if (crcPayload == crcMessage) {
		printf("Received: %s", message);
		printf("Payload: %s", payload);

		return true;
	}else{
		return false;
	}
}


int32_t packSendPayload(dataPackage *data, uint8_t * payload, int32_t lenPay) {

	uint16_t crcPayload = crc16(payload, lenPay);
	int32_t count = 0;
	uint8_t messageSend[256];
	
	if (lenPay <= 256)
	{
		messageSend[count++] = 2;
		messageSend[count++] = lenPay;
	}
	else
	{
		messageSend[count++] = 3;
		messageSend[count++] = (uint8_t)(lenPay >> 8);
		messageSend[count++] = (uint8_t)(lenPay & 0xFF);
	}

	memcpy(messageSend + count, payload, lenPay);
	count += lenPay;

	messageSend[count++] = (uint8_t)(crcPayload >> 8);
	messageSend[count++] = (uint8_t)(crcPayload & 0xFF);
	messageSend[count++] = 3;
	// messageSend[count] = NULL;
	
	printf("Package to send: %s", messageSend);

	// Sending package
	int result = uart_write_bytes(UART_NUM, &messageSend, count);
	if (result < 0) {
    	ESP_LOGE(VESC_UART_TAG, "Failed to write to UART, error: %d", result);
	}
	// Returns number of send bytes
	return count;
}


bool processReadPacket(dataPackage *data, uint8_t * message) {

	COMM_PACKET_ID packetId;
	int32_t index = 0;

	packetId = (COMM_PACKET_ID)message[0];
	message++; // Removes the packetId from the actual message (payload)

	switch (packetId){
		case COMM_FW_VERSION: // Structure defined here: https://github.com/vedderb/bldc/blob/43c3bbaf91f5052a35b75c2ff17b5fe99fad94d1/commands.c#L164

			fw_version.major = message[index++];
			fw_version.minor = message[index++];
			return true;
		case COMM_GET_VALUES: // Structure defined here: https://github.com/vedderb/bldc/blob/43c3bbaf91f5052a35b75c2ff17b5fe99fad94d1/commands.c#L164

			data->tempMosfet 		= buffer_get_float16(message, 10.0, &index); 	// 2 bytes - mc_interface_temp_fet_filtered()
			data->tempMotor 			= buffer_get_float16(message, 10.0, &index); 	// 2 bytes - mc_interface_temp_motor_filtered()
			data->avgMotorCurrent 	= buffer_get_float32(message, 100.0, &index); // 4 bytes - mc_interface_read_reset_avg_motor_current()
			data->avgInputCurrent 	= buffer_get_float32(message, 100.0, &index); // 4 bytes - mc_interface_read_reset_avg_input_current()
			index += 4; // Skip 4 bytes - mc_interface_read_reset_avg_id()
			index += 4; // Skip 4 bytes - mc_interface_read_reset_avg_iq()
			data->dutyCycleNow 		= buffer_get_float16(message, 1000.0, &index); 	// 2 bytes - mc_interface_get_duty_cycle_now()
			data->rpm 				= buffer_get_float32(message, 1.0, &index);		// 4 bytes - mc_interface_get_rpm()
			data->inpVoltage 		= buffer_get_float16(message, 10.0, &index);		// 2 bytes - GET_INPUT_VOLTAGE()
			data->ampHours 			= buffer_get_float32(message, 10000.0, &index);	// 4 bytes - mc_interface_get_amp_hours(false)
			data->ampHoursCharged 	= buffer_get_float32(message, 10000.0, &index);	// 4 bytes - mc_interface_get_amp_hours_charged(false)
			data->wattHours			= buffer_get_float32(message, 10000.0, &index);	// 4 bytes - mc_interface_get_watt_hours(false)
			data->wattHoursCharged	= buffer_get_float32(message, 10000.0, &index);	// 4 bytes - mc_interface_get_watt_hours_charged(false)
			data->tachometer 		= buffer_get_int32(message, &index);				// 4 bytes - mc_interface_get_tachometer_value(false)
			data->tachometerAbs 		= buffer_get_int32(message, &index);				// 4 bytes - mc_interface_get_tachometer_abs_value(false)
			data->error 				= (mc_fault_code)message[index++];								// 1 byte  - mc_interface_get_fault()
			data->pidPos				= buffer_get_float32(message, 1000000.0, &index);	// 4 bytes - mc_interface_get_pid_pos_now()
			data->id					= message[index++];								// 1 byte  - app_get_configuration()->controller_id	

			return true;

		break;

		/* case COMM_GET_VALUES_SELECTIVE:

			uint32_t mask = 0xFFFFFFFF; */

		default:
			return false;
		break;
	}
}

bool getFWversion(dataPackage *data){
	return getFWversionCAN(data, 0);
}

bool getFWversionCAN(dataPackage *data, uint8_t canId){
	
	int32_t index = 0;
	int32_t payloadSize = (canId == 0 ? 1 : 3);
	uint8_t payload[payloadSize];
	
	if (canId != 0) {
		payload[index++] = COMM_FORWARD_CAN;
		payload[index++] = canId;
	}
	payload[index++] = COMM_FW_VERSION;

	packSendPayload(data, payload, payloadSize);

	uint8_t message[256];
	int32_t messageLength = receiveUartMessage(data, message);
	if (messageLength > 0) { 
		return processReadPacket(data, message); 
	}
	return false;
}

bool getVescValues(dataPackage* data) {
	return getVescValuesCAN(data, 0);
}

bool getVescValuesCAN(dataPackage *data, uint8_t canId) {

	printf("Command: COMM_GET_VALUES %c", canId);

	int32_t index = 0;
	int32_t payloadSize = (canId == 0 ? 1 : 3);
	uint8_t payload[payloadSize];
	if (canId != 0) {
		payload[index++] = COMM_FORWARD_CAN;
		payload[index++] = canId;
	}
	payload[index++] = COMM_GET_VALUES;

	packSendPayload(data, payload, payloadSize);

	uint8_t message[256];
	int32_t messageLength = receiveUartMessage(data, message);

	if (messageLength > 55) {
		return processReadPacket(data, message); 
	}
	return false;
}

void setCurrent(dataPackage *data, float current) {
	return setCurrentCAN(data, current, 0);
}

void setCurrentCAN(dataPackage *data, float current, uint8_t canId) {
	int32_t index = 0;
	int32_t payloadSize = (canId == 0 ? 5 : 7);
	uint8_t payload[payloadSize];
	if (canId != 0) {
		payload[index++] = COMM_FORWARD_CAN;
		payload[index++] = canId;
	}
	payload[index++] = COMM_SET_CURRENT;
	buffer_append_int32(payload, (int32_t)(current * 1000), &index);
	packSendPayload(data, payload, payloadSize);
}

void setBrakeCurrent(dataPackage *data, float brakeCurrent) {
	return setBrakeCurrentCAN(data, brakeCurrent, 0);
}

void setBrakeCurrentCAN(dataPackage *data, float brakeCurrent, uint8_t canId) {
	int32_t index = 0;
	int32_t payloadSize = (canId == 0 ? 5 : 7);
	uint8_t payload[payloadSize];
	if (canId != 0) {
		payload[index++] = COMM_FORWARD_CAN;
		payload[index++] = canId;
	}

	payload[index++] = COMM_SET_CURRENT_BRAKE;
	buffer_append_int32(payload, (int32_t)(brakeCurrent * 1000), &index);

	packSendPayload(data, payload, payloadSize);
}

void setRPM(dataPackage *data, float rpm) {
	return setRPMCAN(data, rpm, 0);
}

void setRPMCAN(dataPackage *data, float rpm, uint8_t canId) {
	int32_t index = 0;
	int32_t payloadSize = (canId == 0 ? 5 : 7);
	uint8_t payload[payloadSize];
	if (canId != 0) {
		payload[index++] = COMM_FORWARD_CAN;
		payload[index++] = canId;
	}
	payload[index++] = COMM_SET_RPM;
	buffer_append_int32(payload, (int32_t)(rpm), &index);
	packSendPayload(data, payload, payloadSize);
}

void setDuty(dataPackage *data, float duty) {
	return setDutyCAN(data, duty, 0);
}

void setDutyCAN(dataPackage *data, float duty, uint8_t canId) {
	int32_t index = 0;
	int32_t payloadSize = (canId == 0 ? 5 : 7);
	uint8_t payload[payloadSize];
	if (canId != 0) {
		payload[index++] = COMM_FORWARD_CAN;
		payload[index++] = canId;
	}
	payload[index++] = COMM_SET_DUTY;
	buffer_append_int32(payload, (int32_t)(duty * 100000), &index);

	packSendPayload(data, payload, payloadSize);
}

void sendKeepalive(dataPackage *data) {
	return sendKeepaliveCAN(data, 0);
}

void sendKeepaliveCAN(dataPackage *data, uint8_t canId) {
	int32_t index = 0;
	int32_t payloadSize = (canId == 0 ? 1 : 3);
	uint8_t payload[payloadSize];
	if (canId != 0) {
		payload[index++] = COMM_FORWARD_CAN;
		payload[index++] = canId;
	}
	payload[index++] = COMM_ALIVE;
	packSendPayload(data, payload, payloadSize);
}