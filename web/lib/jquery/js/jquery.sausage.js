//     jquery.sausage.js 1.0.0
//     (c) 2011 Christopher Cliff
//     Freely distributed under the MIT license.
//     For all details and documentation:
//     http://christophercliff.github.com/sausage

(function($) {

	$.widget('cc.sausage', {

		options: {
			page: function() {
				$('body').find('.page')
			},
			content: function (i) {
				return '<span class="sausage-span">' + (i + 1) + '</span>';
			}
		},

		// # Private Methods

		_create: function () {
			var self = this
			self.sausageContainer = self.element
			self.contentContainer = self.options.container
			self.$sausages = $('<div class="sausage-set"/>');
			self.sausages = self.$sausages.get(0);
			self.offsets = [];
			self.$sausages.appendTo(self.sausageContainer);
			// Trigger the `create` event.
			self._trigger('create');
		},
		_init: function () {
			var self = this;
			self.draw();
			self._events();
			self._delegates();

			// Add a CSS class for styling purposes.
			self.$sausages.addClass('sausage-set-init')
			self.blocked = false;
			self._trigger('init');
		},
		_events: function () {
			var self = this;
			self.sausageContainer.bind('resize.sausage', function() {
				self.draw();
			})
		},
		_delegates: function () {
			var self = this;
			self.$sausages.delegate('.sausage', 'hover',
					function() {
						if (! self.blocked) {
							$(this).toggleClass('sausage-hover')
						}
					}).delegate('.sausage', 'click', function(e) {
						if (! self.blocked) {
							e.preventDefault();
							self.options.scrollTo($('#' + $(this).attr('target')));
						}
					})
		},
		// # Public Methods
		//
		// Creates the sausage UI elements.
		draw: function () {
			var self = this,
					hWin = self.sausageContainer.height(),
					$items = self.options.page(),
					hDoc = 0,
					s = [];
			self.offsets = []
			$items.each(function() {
				var height = $(this).outerHeight()
				hDoc += height
				self.offsets.push(height)
			});
			self.count = $items.length;
			var hRatio = hWin / hDoc

			// Detach from DOM while making changes.
			self.$sausages.detach().empty()

			for (var i = 0; i < self.count; i++) {
				var $page = $items.eq(i);
				var height = self.offsets[i] * hRatio

				s.push('<div class="sausage' + ((i === self.current) ? ' sausage-current' : '') + '" style="height:' + height + 'px;" target="' + $page.attr('id') + '">' + self.options.content(i, $page) + '</div>');
			}
			// Use Array.join() for speed.
			self.sausages.innerHTML = s.join('');
			// And reattach.
			self.$sausages.appendTo(self.sausageContainer)
		},

		// ### block `.sausage("block")`
		//
		// Blocks the UI to prevent users from interacting with the sausage UI. Useful when loading data and updating the DOM.
		block: function () {
			var self = this, c = 'sausage-set-blocked';
			self.blocked = true;
			// Add a CSS class for styling purposes.
			self.$sausages.addClass(c)
		},
		// ### unblock `.sausage("unblock")`
		//
		// Unblocks the UI once loading and DOM manipulation are complete.
		unblock: function () {
			var self = this, c = 'sausage-set-blocked';
			self.$sausages.removeClass(c)
			self.blocked = false;
		},
		// ### destroy `.sausage("destroy")`
		//
		// Removes the sausage instance from the DOM.
		destroy: function () {
			var self = this;
			self.sausageContainer.unbind('.sausage')
			self.$sausages.remove()
		},
		setFocus : function(section) {
			var id = section.attr('id')
			$('div.sausage', this.$sausages).removeClass('sausage-current').filter('[target=' + id + ']').addClass('sausage-current')
		}
	});
})(jQuery)