/*
    Copyright (C) 2025 Kostya Stern

    This program is free software; you can redistribute it and/or modify it
    under the terms of the GNU General Public License as published by the Free
    Software Foundation; either version 2 of the License, or (at your option)
    any later version.

    This program is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
    more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc., 51
    Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
*/
package com.sternkn.djvu.file.coders;

/*
   p -  the amount by which the current arithmetic coding interval is reduced if the decoded symbol is the MPS.
   m -  the threshold above which an MPS triggers a probability state update.
   up - the next probability state index for context k after an MPS triggers a probability state index update.
        An LPS always triggers a probability state index update.
   dn - the next probability state index for context k after an LPS.
 */
public record ZpCodecTable(int p, int m, int up, int dn) {

}
