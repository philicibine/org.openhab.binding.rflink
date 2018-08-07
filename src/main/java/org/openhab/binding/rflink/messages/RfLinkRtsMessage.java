/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rflink.messages;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.Type;
import org.openhab.binding.rflink.RfLinkBindingConstants;
import org.openhab.binding.rflink.config.RfLinkDeviceConfiguration;
import org.openhab.binding.rflink.exceptions.RfLinkException;
import org.openhab.binding.rflink.exceptions.RfLinkNotImpException;

/**
 * RfLink data class for Somfy/RTS message.
 *
 * @author John Jore - Initial contribution
 * @author Arjan Mels - Added reception and debugged sending
 */
public class RfLinkRtsMessage extends RfLinkBaseMessage {
    private static final String KEY_RTS = "RTS";
    private static final Collection<String> KEYS = Arrays.asList(KEY_RTS);

    public Command command = null;
    public UpDownType state = null;

    public RfLinkRtsMessage() {
    }

    public RfLinkRtsMessage(String data) {
        encodeMessage(data);
    }

    @Override
    public ThingTypeUID getThingType() {
        return RfLinkBindingConstants.THING_TYPE_RTS;
    }

    @Override
    public String toString() {
        String str = "";
        str += super.toString();
        str += ", State = " + state;
        str += ", Command = " + command;
        return str;
    }

    @Override
    public void encodeMessage(String data) {
        super.encodeMessage(data);
    }

    @Override
    public Collection<String> keys() {
        return KEYS;
    }

    @Override
    public Map<String, State> getStates() {
        Map<String, State> map = new HashMap<>();
        map.put(RfLinkBindingConstants.CHANNEL_SHUTTER, state);
        return map;
    }

    @Override
    public void initializeFromChannel(RfLinkDeviceConfiguration config, ChannelUID channelUID, Command triggeredCommand)
            throws RfLinkNotImpException, RfLinkException {
        super.initializeFromChannel(config, channelUID, triggeredCommand);
        attachCommandAction(channelUID.getId(), triggeredCommand);
    }

    @Override
    public String decodeMessageAsString(String suffix) {
        return super.decodeMessageAsString(this.command.toString());
    }

    public String getEffectiveCommand() {
        return this.command.toString();
    }

    public void attachCommandAction(String channelId, Type type) throws RfLinkException {
        switch (channelId) {
            case RfLinkBindingConstants.CHANNEL_COMMAND:
            case RfLinkBindingConstants.CHANNEL_SHUTTER:
                if (type instanceof OpenClosedType) {
                    this.command = (type == OpenClosedType.CLOSED ? UpDownType.DOWN : UpDownType.UP);
                    this.state = (UpDownType) command;
                } else if (type instanceof UpDownType) {
                    this.command = (UpDownType) type;
                    this.state = (UpDownType) command;
                } else if (type instanceof OnOffType) {
                    this.command = (Command) type;
                    this.state = OnOffType.ON.equals(command) ? UpDownType.DOWN : UpDownType.UP;
                } else if (type instanceof StopMoveType) {
                    command = StopMoveType.STOP;
                } else if (type instanceof PercentType) {
                    int value = ((PercentType) type).intValue();
                    if (value > 50) {
                        command = UpDownType.DOWN;
                    } else {
                        command = UpDownType.UP;
                    }
                } else {
                    throw new RfLinkException("Channel " + channelId + " does not accept " + type);
                }
                break;
            default:
                throw new RfLinkException("Channel " + channelId + " is not relevant here");
        }
    }
}
