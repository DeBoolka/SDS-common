package ru.mirea.dikanev.nikita.common.server.service.connector;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.HashSet;
import java.util.Set;

import ru.mirea.dikanev.nikita.common.server.connector.ChannelConnector;
import ru.mirea.dikanev.nikita.common.server.exception.AuthenticationException;
import ru.mirea.dikanev.nikita.common.server.handler.CellManagerHandler;

public class CellManagerConnectorService extends SimpleConnectorService {

    private Set<SelectableChannel> cellAcceptableChannels = new HashSet<>();

    public CellManagerConnectorService(CellManagerHandler handler) {
        super(handler);
    }

    @Override
    public void closeConnection(SelectionKey key, SelectableChannel channel) {
        cellAcceptableChannels.remove(channel);
        super.closeConnection(key, channel);
    }

    @Override
    public void accept(SelectionKey key, ChannelConnector connector) throws IOException {
        ChannelConnector newConnector = null;
        try {
            newConnector = connector.onAccept(handler.selector(), handler);
            if (isCellAcceptableChannel(connector.getChannel())) {
                ((CellManagerHandler) handler).newCell(newConnector);
            }
            handler.bind(newConnector);
        } catch (AuthenticationException ignore) {
            newConnector.getChannel().close();
        }
    }

    @Override
    public void clear() {
        cellAcceptableChannels.clear();
        super.clear();
    }

    public boolean isCellAcceptableChannel(SelectableChannel channel) {
        return cellAcceptableChannels.contains(channel);
    }

    public void setCellAcceptableChannel(SelectableChannel channel) {
        cellAcceptableChannels.add(channel);
    }
}
