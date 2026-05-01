async function getMenu() {
  const res = await fetch('http://localhost/graphql/execute.json/cheesecake/menu', {
    cache: 'no-store'
  });
  return res.json();
}

async function getEvents() {
  const res = await fetch('http://localhost/graphql/execute.json/cheesecake/events', {
    cache: 'no-store'
  });
  return res.json();
}

export default async function Home() {
  const menuData = await getMenu();
  const eventsData = await getEvents();

  return (
    <main className="max-w-3xl mx-auto p-8">
      <h1 className="text-3xl font-bold mb-8">Aakash Japanese Cheesecake</h1>

      <section className="mb-12">
        <h2 className="text-2xl font-semibold mb-4">Menu</h2>
        {menuData.data.menuItems.map((item: any) => (
          <div key={item.title} className="border p-4 mb-4 rounded">
            <h3 className="text-xl font-medium">{item.title}</h3>
            <p className="text-gray-600">{item.description}</p>
            <p className="text-lg font-bold mt-2">${item.price}</p>
          </div>
        ))}
      </section>

      <section>
        <h2 className="text-2xl font-semibold mb-4">Upcoming Events</h2>
        {eventsData.data.events.map((event: any) => (
          <div key={event.title} className="border p-4 mb-4 rounded">
            <h3 className="text-xl font-medium">{event.title}</h3>
            <p className="text-gray-600">{event.location}</p>
            <p className="text-gray-600">{event.date} · {event.hours}</p>
          </div>
        ))}
      </section>
    </main>
  );
}