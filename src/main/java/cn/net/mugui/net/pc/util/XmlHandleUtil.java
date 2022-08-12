package cn.net.mugui.net.pc.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class XmlHandleUtil {
	/**
	 * 查询节点
	 * @param document
	 * @param nodeName
	 */
	public static List<Node> findNode(Document document, String nodeName){

		LinkedList<Node> data = new LinkedList<Node>();

		if(document==null)return data;
		String[] split = nodeName.split("[.]");
		if(split.length<=0) return data;

		List<Node> nodes = new LinkedList<Node>();
		nodes.add(document);
		for(String key : split){
			List<Node> nodeOne = findNodeOne(nodes, key);
			if(nodeOne.isEmpty()){
				return data;
			}
			nodes=nodeOne;
		}
		return nodes;
	}

	/**
	 * 查询单节点
	 * @param document
	 * @param key
	 */
	public static List<Node> findNodeOne(List<Node> document, String key) {
		List<Node> nodes = new LinkedList<Node>();
		for(Node node:document){
			NodeList childNodes = node.getChildNodes();
			for(int i=0; i<childNodes.getLength();i++){
				boolean key1 = childNodes.item(i).getNodeName().equals(key);
				if(key1){
					nodes.add(childNodes.item(i));
				}
			}
		}
		return nodes;
	}
}
