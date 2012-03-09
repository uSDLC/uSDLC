package usdlc

import static usdlc.config.Config.config

class News {
	/**
	 * Must load items in the constructor so we can ignore network errors.
	 * Note that we only read updates from the net with a new instance of News.
	 */
	News() { load() }
	/**
	 * Expose the load method so we can update the news if we ever need to -
	 * and retry when it fails. New news is picked up on program start and
	 * every day thereafter.
	 */
	public load() {
		parser = new XmlParser()
		if (!items || timer.days)
			items = []
			try {
				index = -1
				def text = new URL(config.newsUrl).text
				items = parser.parseText(text).channel[0].item
				if (items.size()) {
					cache.headline = headline
					cache.description = description
					index = -1
					timer = new Timer()
				}
			} catch (e) {
			}
	}
	XmlParser parser
	/**
	 * If done the first time returns the first item - otherwise goes for the
	 * next, wrapping when we run out of news.
	 * @return
	 */
	News getNext() {
		if (!items) load()
		index = (index + 1) % items.size()
		this
	}
	/**
	 * The headline is the title given to the blog
	 */
	String getHeadline() {
		def title = item.title.text()
		def link = item.link.text()
		"<a href='$link' target='_blank'>$title</a>"
	}
	/**
	 * The description is a form of synopsis - taken from the body with [...]
	 * to click on for more.
	 */
	String getDescription() {
		item.description.text()
	}
	/**
	 * The items are read from the server whenever News is instantiated.
	 */
	static items
	def index = -1

	def getItem() {
		if (index == -1) index = 0
		def empty = [text: {''}]
		items.size() ? items[index] :
			[title: empty, link: empty, content: empty]
	}

	static cache = [headline: '', description: '']
	static timer = new Timer()
}
