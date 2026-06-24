import socket
import time

# --- Helper Functions ---

def get_local_ip():
    """
    Retrieves the local IP address of the machine running this script.
    It uses a dummy UDP connection to a public DNS (Google's 8.8.8.8) 
    to determine which network interface is routing the traffic.
    """
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:
        # We don't actually need to establish a connection, 
        # just attempting it allows the OS to assign a local IP.
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        return ip
    except Exception:
        # Fallback to localhost if there's no active network connection
        return "127.0.0.1"
    finally:
        # Always close the socket to free up system resources
        s.close()

# --- Server Configuration ---

# Get the local IP to bind the server
UDP_IP = get_local_ip()
UDP_PORT = 8888  # The port our Android app is communicating with

# Create a UDP socket (IPv4, Datagram)
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)

# Bind the socket to the IP and Port
sock.bind((UDP_IP, UDP_PORT))

# Set a timeout of 5 seconds for the socket.
# This is crucial: it prevents the recvfrom() method from blocking indefinitely,
# allowing the server to handle exceptions or be interrupted gracefully.
sock.settimeout(5)

print(f"UDP Mock Server is UP and listening on {UDP_IP}:{UDP_PORT}...\n")


# --- Mock Crane Data ---

# Simulating the telemetry data sent by the actual crane hardware.
# Fields include: workClock, oilIn, oilOut, tower, fixF, fixB, rot1, rot2, 
# gbMs, filter, pressure, weight, belt, voltage, clutch, grease, hydrolic, etc.
statusMessage = "status:112390,126.8,9.3,1,1,1,0,0,0,1,1,1,1,1,1,1,1,108290,108290,3631.8782N,05259.7112E,13.6,"

# Simulating alarm flags
alarmMessage = "alarm:0-0,0-0,3640-5,0-0,3649-5,0-0,0-0,0-0,0-0,4250-4,0-0,4255-1,0-0,0-0,0-0,0-0,0-0,0-0,4367-1,"

# The complete payload matching the real hardware's response format
statusResponse = statusMessage + alarmMessage


# --- Main Server Loop ---

while True:
    try:
        # Wait for incoming UDP packets. 
        # 1024 is the buffer size in bytes.
        data, addr = sock.recvfrom(1024)
        
        # Decode the byte array to string and remove whitespace/newlines
        message = data.decode().strip()
        print(f"New message from {addr}: '{message}'")

        # --- Command Processing ---

        # Handle initial connection request from the Android app
        if message.startswith("connect_request:"):
            response = f"connect_ok:{UDP_IP}"
            sock.sendto(response.encode(), addr)
            print(f"Client connected! Sent response: '{response}'")

        # Handle graceful disconnection
        elif message.startswith("disconnect_request"):
            response = "disconnect_ok"
            sock.sendto(response.encode(), addr)
            print(f"🔌 Client requested disconnect.")

        # Handle periodic status polling
        elif message.startswith("status"):
            response = statusResponse
            sock.sendto(response.encode(), addr)
            print(f"Crane status payload sent.")

        # Handle remote control commands (joystick/keys)
        elif message.startswith("key:"):
            # For the mock server, we just echo back the status to confirm receipt
            response = statusMessage
            sock.sendto(response.encode(), addr)
            print(f"Key command received and acknowledged.")
        
        # Handle log deletion request
        elif message.startswith("delete_logs"):
            response = "delete_logs_ok"
            sock.sendto(response.encode(), addr)
            print(f"Logs deletion acknowledged.")

        # Ignore unknown packets
        else:
            print(f"Unknown command received: '{message}'")
            pass

    # --- Error Handling ---

    except socket.timeout:
        # This is expected behavior due to sock.settimeout(5).
        # We simply pass and let the loop continue waiting for packets.
        pass

    except Exception as e:
        # Catch any unexpected errors to prevent the server from crashing.
        # Sleep for 1 second to prevent rapid error looping.
        print(f"Unexpected error occurred: {e}")
        time.sleep(1)
