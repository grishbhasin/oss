/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2012-2013 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/

package com.jaeksoft.searchlib.parser.htmlParser;

import java.util.Collection;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaeksoft.searchlib.util.DomUtils;
import com.jaeksoft.searchlib.util.XPathParser;

public class DomHtmlNode extends HtmlNodeAbstract<Node> implements
		HtmlDocumentProvider.XPath {

	private XPathParser xPathParser = null;

	public DomHtmlNode(Node node) {
		super(node);
	}

	@Override
	final public int countElements() {
		return DomUtils.countElements(node);
	}

	@Override
	public String getFirstTextNode(String... path) {
		return DomUtils.getFirstTextNode(node, path);
	}

	@Override
	public String getText() {
		return DomUtils.getText(node);
	}

	@Override
	public String getAttributeText(String name) {
		return DomUtils.getAttributeText(node, name);
	}

	@Override
	public void getNodes(List<HtmlNodeAbstract<?>> nodes, String... path) {
		List<Node> nodeList = DomUtils.getNodes(node, path);
		for (Node node : nodeList)
			nodes.add(new DomHtmlNode(node));
	}

	@Override
	public List<HtmlNodeAbstract<?>> getAllNodes(String... tags) {
		List<HtmlNodeAbstract<?>> nodes = getNewNodeList();
		List<Node> nodeList = DomUtils.getAllNodes(node, tags);
		for (Node node : nodeList)
			nodes.add(new DomHtmlNode(node));
		return nodes;
	}

	@Override
	protected List<HtmlNodeAbstract<?>> getNewChildNodes() {
		List<HtmlNodeAbstract<?>> nodes = getNewNodeList();
		NodeList nodeList = node.getChildNodes();
		int l = nodeList.getLength();
		for (int i = 0; i < l; i++)
			nodes.add(new DomHtmlNode(nodeList.item(i)));
		return nodes;
	}

	@Override
	public boolean isComment() {
		return node.getNodeType() == Node.COMMENT_NODE;
	}

	@Override
	public boolean isTextNode() {
		return node.getNodeType() == Node.TEXT_NODE;
	}

	@Override
	public String getNodeName() {
		return node.getNodeName();
	}

	@Override
	public String getAttribute(String name) {
		return DomUtils.getAttributeText(node, name);
	}

	@Override
	public void xPath(String xPath, Collection<Object> nodes)
			throws XPathExpressionException {
		if (xPathParser == null)
			xPathParser = new XPathParser(node);
		Object obj = xPathParser.evaluate(node, xPath, XPathConstants.NODESET);
		if (obj == null)
			return;
		if (obj instanceof Node) {
			nodes.add(new DomHtmlNode((Node) obj));
		} else if (obj instanceof NodeList) {
			NodeList nodeList = (NodeList) obj;
			int length = nodeList.getLength();
			for (int i = 0; i < length; i++)
				nodes.add(nodeList.item(i));
		}
	}
}
