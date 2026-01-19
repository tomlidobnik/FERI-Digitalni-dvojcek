"""
MQTT Listener Service for Event Sensor Data
Subscribes to event/{event_id}/sensor_type topics and saves data to database
"""

import paho.mqtt.client as mqtt
import json
import time
import os
from datetime import datetime
from database import get_db_connection
from dotenv import load_dotenv

load_dotenv()

# MQTT Configuration
BROKER_HOST = os.getenv("MQTT_BROKER_HOST", "mosquitto")
BROKER_PORT = int(os.getenv("MQTT_BROKER_PORT", "1883"))
TOPIC_PATTERN = "event/#"

# Supported sensor types
SENSOR_TYPES = ["sound_level", "temperature", "humidity", "light", "motion", "co2"]


def save_sensor_data(event_id: int, sensor_type: str, value: str):
    """Save sensor data to the database"""
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        
        timestamp = datetime.now().isoformat()
        
        cursor.execute(
            """
            INSERT INTO event_sensor_data (event_fk, sensor_type, value, timestamp)
            VALUES (?, ?, ?, ?)
            """,
            (event_id, sensor_type, value, timestamp)
        )
        
        conn.commit()
        conn.close()
        
        print(f"✓ Saved {sensor_type}={value} for event {event_id} at {timestamp}")
        return True
    except Exception as e:
        print(f"✗ Error saving sensor data: {e}")
        return False


def on_connect(client, userdata, flags, reason_code, properties):
    """Callback when client connects to broker"""
    if reason_code == 0:
        print(f"✓ Connected to MQTT broker at {BROKER_HOST}:{BROKER_PORT}")
        client.subscribe(TOPIC_PATTERN)
        print(f"✓ Subscribed to topic pattern: {TOPIC_PATTERN}")
    else:
        print(f"✗ Connection failed with code: {reason_code}")


def on_message(client, userdata, msg):
    """Callback when a message is received"""
    try:
        topic = msg.topic
        payload = msg.payload.decode()
        
        print(f"📨 Received on '{topic}': {payload}")
        
        # Parse topic: event/{event_id}/{sensor_type}
        parts = topic.split("/")
        if len(parts) != 3 or parts[0] != "event":
            print(f"⚠ Skipping invalid topic format: {topic}")
            return
        
        event_id = parts[1]
        sensor_type = parts[2]
        
        # Validate event_id is a number
        try:
            event_id = int(event_id)
        except ValueError:
            print(f"⚠ Invalid event_id: {event_id}")
            return
        
        # Validate sensor type
        if sensor_type not in SENSOR_TYPES:
            print(f"⚠ Unknown sensor type: {sensor_type}")
            return
        
        # Try to parse as JSON first, otherwise use raw value
        try:
            data = json.loads(payload)
            # If it's a dict, convert to JSON string for storage
            value = json.dumps(data) if isinstance(data, dict) else str(data)
        except json.JSONDecodeError:
            # Use raw payload if not JSON
            value = payload
        
        # Save to database
        save_sensor_data(event_id, sensor_type, value)
        
        # Check for loud noise alert (sound_level > 100 dB)
        if sensor_type == "sound_level":
            try:
                sound_value = float(payload)
                if sound_value > 100:
                    # Get event title from database
                    conn = get_db_connection()
                    cursor = conn.cursor()
                    cursor.execute("SELECT title FROM events WHERE id = ?", (event_id,))
                    event_row = cursor.fetchone()
                    event_title = event_row["title"] if event_row else f"Event {event_id}"
                    conn.close()
                    
                    alert_message = json.dumps({
                        "event_id": event_id,
                        "event_title": event_title,
                        "alert_type": "loud_noise",
                        "sound_level": sound_value,
                        "timestamp": datetime.now().isoformat(),
                        "message": f"Loud noise detected at {event_title}: {sound_value} dB"
                    })
                    
                    result = client.publish("announcement/global", alert_message, qos=1)
                    print(f"🚨 ALERT: Loud noise ({sound_value} dB) at {event_title} - Published to announcement/global")
            except (ValueError, TypeError) as e:
                print(f"⚠ Could not parse sound level value: {payload}")
        
    except Exception as e:
        print(f"✗ Error processing message: {e}")


def on_disconnect(client, userdata, flags, reason_code, properties):
    """Callback when client disconnects"""
    print(f"⚠ Disconnected from MQTT broker (code: {reason_code})")
    if reason_code != 0:
        print("⚠ Unexpected disconnection. Will attempt to reconnect...")


def on_subscribe(client, userdata, mid, reason_code_list, properties):
    """Callback when subscription is confirmed"""
    print(f"✓ Subscription confirmed")


def main():
    """Main function to start the MQTT listener"""
    print("="*60)
    print("MQTT Event Sensor Listener")
    print("="*60)
    print(f"Broker: {BROKER_HOST}:{BROKER_PORT}")
    print(f"Topic Pattern: {TOPIC_PATTERN}")
    print(f"Supported Sensors: {', '.join(SENSOR_TYPES)}")
    print("="*60)
    
    # Create MQTT client
    client = mqtt.Client(
        callback_api_version=mqtt.CallbackAPIVersion.VERSION2,
        client_id="event_sensor_listener",
        protocol=mqtt.MQTTv5
    )
    
    # Assign callbacks
    client.on_connect = on_connect
    client.on_message = on_message
    client.on_disconnect = on_disconnect
    client.on_subscribe = on_subscribe
    
    # Connect with retry logic
    max_retries = 5
    retry_delay = 5
    
    for attempt in range(max_retries):
        try:
            print(f"🔄 Connecting to broker (attempt {attempt + 1}/{max_retries})...")
            client.connect(BROKER_HOST, BROKER_PORT, keepalive=60)
            break
        except Exception as e:
            print(f"✗ Connection failed: {e}")
            if attempt < max_retries - 1:
                print(f"⏳ Retrying in {retry_delay} seconds...")
                time.sleep(retry_delay)
            else:
                print("✗ Max retries reached. Exiting.")
                return
    
    # Start network loop
    print("🎧 Listening for sensor data...")
    print("Press Ctrl+C to stop\n")
    
    try:
        client.loop_forever()
    except KeyboardInterrupt:
        print("\n\n⏹ Stopping listener...")
    finally:
        client.disconnect()
        print("✓ Disconnected. Goodbye!")


if __name__ == "__main__":
    main()
