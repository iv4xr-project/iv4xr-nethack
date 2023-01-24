"""
Listen for client connections and dispatch handlers.
"""

import os
import sys
import subprocess
import socket
import logging

if sys.version_info >= (3, 0):
    import socketserver
else:
    import SocketServer as socketserver


def serve(port=5001, universe=False, setup_code=''):
    """
    Run a server on the given port.
    """
    logging.info(f"Starting server (port={port})")
    server = Server(('127.0.0.1', port), Handler)
    server.universe = universe
    server.setup_code = setup_code
    logging.info(f"Listening on port {port}...")
    server.serve_forever()


class Server(socketserver.ThreadingMixIn, socketserver.TCPServer):
    """
    The connection server.
    """
    allow_reuse_address = True
    universe = False
    setup_code = ''


class Handler(socketserver.BaseRequestHandler):
    """
    The connection handler.
    """
    def handle(self):
        script_file = os.path.join(os.path.dirname(__file__), 'handler.py')
        args = [
            sys.executable,
            script_file,
            '--addr',
            str(self.client_address),
            '--fd',
            str(self.request.fileno()),
            '--setup',
            str(self.server.setup_code)
        ]

        if self.server.universe:
            args.append('--universe')

        logging.debug(f'Initialize handler with command: {"".join(args)}')
        # Greatly reduces latency on Linux.
        if sys.platform in ['linux', 'linux2', 'darwin']:
            self.request.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)

        try:
            logging.info('Connection from ' + str(self.client_address))
            if sys.version_info >= (3, 2):
                proc = subprocess.Popen(args,
                                        stdin=sys.stdin,
                                        stdout=sys.stdout,
                                        stderr=sys.stderr,
                                        pass_fds=(self.request.fileno(),))
            else:
                proc = subprocess.Popen(args,
                                        stdin=sys.stdin,
                                        stdout=sys.stdout,
                                        stderr=sys.stderr,
                                        close_fds=False)
            proc.wait()
        finally:
            logging.info(f"Disconnected from {self.client_address}")
