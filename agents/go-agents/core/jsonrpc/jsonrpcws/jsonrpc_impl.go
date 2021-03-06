//
// Copyright (c) 2012-2017 Codenvy, S.A.
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//   Codenvy, S.A. - initial API and implementation
//

// Package jsonrpcws provides implementation of jsonrpc.NativeConn based on websocket.
//
// The example:
//
// Client:
//	conn, err := jsonrpcws.Dial("ws://host:port/path")
//	if err != nil {
//      	panic(err)
//      }
// 	tunnel := jsonrpc.NewTunnel(conn, jsonrpc.DefaultRouter)
//	tunnel.Go()
//	tunnel.SayHello()
//
// Server:
//	conn, err := jsonrpcws.Upgrade(w, r)
//	if err != nil {
//      	panic(err)
//      }
//	tunnel := jsonrpc.NewTunnel(conn, jsonrpc.DefaultRouter)
//	tunnel.Go()
//	tunnel.SayHello()
package jsonrpcws

import (
	"net/http"

	"github.com/eclipse/che-lib/websocket"
	"github.com/eclipse/che/agents/go-agents/core/jsonrpc"
)

var (
	defaultUpgrader = &websocket.Upgrader{
		ReadBufferSize:  1024,
		WriteBufferSize: 1024,
		CheckOrigin: func(r *http.Request) bool {
			return true
		},
	}
)

// Dial establishes a new client WebSocket connection.
func Dial(url string) (*NativeConnAdapter, error) {
	conn, _, err := websocket.DefaultDialer.Dial(url, nil)
	if err != nil {
		return nil, err
	}
	return &NativeConnAdapter{conn: conn}, nil
}

// Upgrade upgrades http connection to WebSocket connection.
func Upgrade(w http.ResponseWriter, r *http.Request) (*NativeConnAdapter, error) {
	conn, err := defaultUpgrader.Upgrade(w, r, nil)
	if err != nil {
		return nil, err
	}
	return &NativeConnAdapter{RequestURI: r.RequestURI, conn: conn}, nil
}

// NativeConnAdapter adapts WebSocket connection to jsonrpc.NativeConn.
type NativeConnAdapter struct {

	// RequestURI is http.Request URI which is set on connection Upgrade.
	RequestURI string

	// A real websocket connection.
	conn *websocket.Conn
}

// Write writes text message to the WebSocket connection.
func (adapter *NativeConnAdapter) Write(data []byte) error {
	return adapter.conn.WriteMessage(websocket.TextMessage, data)
}

// Next reads next text message from the WebSocket connection.
func (adapter *NativeConnAdapter) Next() ([]byte, error) {
	for {
		mType, data, err := adapter.conn.ReadMessage()
		if err != nil {
			if closeErr, ok := err.(*websocket.CloseError); ok {
				return nil, jsonrpc.NewCloseError(closeErr)
			}
			return nil, err
		}
		if mType == websocket.TextMessage {
			return data, nil
		}
	}
}

// Close closes this connection.
func (adapter *NativeConnAdapter) Close() error {
	err := adapter.conn.Close()
	if closeErr, ok := err.(*websocket.CloseError); ok && isNormallyClosed(closeErr.Code) {
		return nil
	}
	return err
}

func isNormallyClosed(code int) bool {
	return code == websocket.CloseGoingAway ||
		code == websocket.CloseNormalClosure ||
		code == websocket.CloseNoStatusReceived
}
