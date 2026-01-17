# MQTT Event Sensor System

This system listens to MQTT topics for event sensor data and automatically saves it to the database.

## Architecture

- **Mosquitto MQTT Broker**: Receives sensor data from devices (Android, IoT sensors, etc.)
- **MQTT Listener Service**: Subscribes to event topics and saves data to the database
- **REST API**: Provides endpoints to retrieve and analyze sensor data

## Topic Pattern

Devices should publish sensor data to topics following this pattern:

```
event/{event_id}/{sensor_type}
```

### Examples:
- `event/1/sound_level` - Sound level data for event ID 1
- `event/1/temperature` - Temperature data for event ID 1
- `event/5/humidity` - Humidity data for event ID 5

### Supported Sensor Types:
- `sound_level` - Sound/noise level
- `temperature` - Temperature readings
- `humidity` - Humidity percentage
- `light` - Light intensity
- `motion` - Motion detection
- `co2` - CO2 levels

## Data Format

The payload can be:
- **Simple value**: `"25.5"` or `"78"`
- **JSON object**: `{"value": 25.5, "unit": "celsius"}`

Both formats will be stored in the database.

## Database Schema

```sql
CREATE TABLE event_sensor_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    event_fk INTEGER NOT NULL,
    sensor_type TEXT NOT NULL,
    value TEXT NOT NULL,
    timestamp TEXT NOT NULL,
    FOREIGN KEY (event_fk) REFERENCES events(id)
);
```

## Getting Started

### 1. Start the Services

Start the MQTT broker and listener using Docker Compose:

```bash
docker-compose up mosquitto mqtt-listener
```

Or start all services:

```bash
docker-compose up
```

### 2. Test the System

Run the test publisher to send sample data:

```bash
cd backend
uv run python mqtt_test_publisher.py
```

This will publish random sensor data to event ID 1.

### 3. View the Data

The listener will automatically save incoming data. Check the listener logs:

```bash
docker-compose logs -f mqtt-listener
```

## API Endpoints

### Get all sensor data for an event
```http
GET /sensor_data/event/{event_id}?sensor_type=temperature&limit=100
```

### Get latest readings for each sensor
```http
GET /sensor_data/event/{event_id}/latest
```

### Get sensor statistics
```http
GET /sensor_data/event/{event_id}/stats?sensor_type=temperature&hours=24
```

## Example API Usage

```bash
# Get latest sensor readings for event 1
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8000/sensor_data/event/1/latest

# Get temperature data for event 1
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8000/sensor_data/event/1?sensor_type=temperature&limit=50

# Get temperature statistics for last 24 hours
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:8000/sensor_data/event/1/stats?sensor_type=temperature&hours=24
```

## Configuration

Environment variables (in `.env` file):

```env
MQTT_BROKER_HOST=localhost  # Use 'mosquitto' in Docker
MQTT_BROKER_PORT=1883
```

## Android Integration

From your Android app, publish sensor data like this:

```kotlin
// Example using Paho MQTT Android client
val client = MqttClient("tcp://your-server:1883", "Android_${System.currentTimeMillis()}")
client.connect()

// Publish sound level
val soundLevel = 65.5f
client.publish("event/1/sound_level", soundLevel.toString().toByteArray(), 1, false)

// Publish temperature
val temperature = 22.3f
client.publish("event/1/temperature", temperature.toString().toByteArray(), 1, false)
```

## Troubleshooting

### Connection Issues
If the listener can't connect to the broker:
1. Ensure Mosquitto is running: `docker-compose ps`
2. Check logs: `docker-compose logs mosquitto`
3. Verify network connectivity between services

### Data Not Saving
1. Check listener logs: `docker-compose logs mqtt-listener`
2. Verify the event ID exists in the database
3. Ensure the topic pattern is correct: `event/{id}/{sensor_type}`

### View Raw MQTT Traffic
Enable verbose logging in Mosquitto config or use mosquitto_sub:

```bash
# Subscribe to all event topics
docker-compose exec mosquitto mosquitto_sub -t "event/#" -v
```

## Development

### Run Listener Locally
```bash
cd backend
export MQTT_BROKER_HOST=localhost
export MQTT_BROKER_PORT=1883
uv run python mqtt_listener.py
```

### Add New Sensor Types
Edit `mqtt_listener.py` and add to the `SENSOR_TYPES` list:

```python
SENSOR_TYPES = ["sound_level", "temperature", "your_new_sensor", ...]
```
