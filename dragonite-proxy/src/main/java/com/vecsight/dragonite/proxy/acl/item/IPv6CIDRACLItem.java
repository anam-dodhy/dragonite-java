/*
 * The Dragonite Project
 * -------------------------
 * See the LICENSE file in the root directory for license information.
 */


package com.vecsight.dragonite.proxy.acl.item;

import com.vecsight.dragonite.proxy.acl.ACLItemMethod;
import com.vecsight.dragonite.proxy.acl.ACLItemType;
import com.vecsight.dragonite.proxy.exception.InvalidAddressException;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPv6CIDRACLItem implements ACLItem {

    private final BigInteger lowest;

    private final BigInteger highest;

    private final ACLItemMethod method;

    private static final byte[] MASK_BASE = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

    public IPv6CIDRACLItem(final String string, final ACLItemMethod method) throws InvalidAddressException {
        this.method = method;

        if (!string.contains("/")) {
            throw new InvalidAddressException(string + " is not a valid IPv6 CIDR address");
        }

        final String[] stringSplit = string.split("/");
        if (stringSplit.length != 2) {
            throw new InvalidAddressException(string + " is not a valid IPv6 CIDR address");
        }

        final byte[] ipBytes;
        try {
            ipBytes = InetAddress.getByName(stringSplit[0]).getAddress();
            if (ipBytes.length != 16) throw new InvalidAddressException(string + " is not a valid IPv6 address");
        } catch (final UnknownHostException e) {
            throw new InvalidAddressException(stringSplit[0] + " is not a valid IPv6 address");
        }

        final int cidrShift;
        try {
            cidrShift = Integer.parseInt(stringSplit[1]);
            if (cidrShift < 1 || cidrShift > 128)
                throw new InvalidAddressException(string + " is not a valid IPv6 CIDR address");
        } catch (final NumberFormatException e) {
            throw new InvalidAddressException(string + " is not a valid IPv4 CIDR address");
        }

        final BigInteger mask = (new BigInteger(1, MASK_BASE)).not().shiftRight(cidrShift);

        final BigInteger ipVal = new BigInteger(1, ipBytes);

        lowest = ipVal.and(mask);

        highest = lowest.add(mask.not());
    }

    @Override
    public ACLItemType getType() {
        return ACLItemType.IPv4_CIDR;
    }

    @Override
    public ACLItemMethod getMethod() {
        return method;
    }

    @Override
    public boolean match(final String domain) {
        return false;
    }

    @Override
    public boolean match(final byte[] address) {
        if (address.length != 16) return false;
        final BigInteger target = new BigInteger(1, address);
        final int st = lowest.compareTo(target);
        final int te = target.compareTo(highest);
        return (st <= 0) && (te <= 0);
    }
}
