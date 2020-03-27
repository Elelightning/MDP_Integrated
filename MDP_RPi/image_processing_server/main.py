from server import ImageProcessingServer


def init():
    try:
        server = ImageProcessingServer()
        server.start()
    except KeyboardInterrupt:
        server.end()

if __name__ == '__main__':
    init()
